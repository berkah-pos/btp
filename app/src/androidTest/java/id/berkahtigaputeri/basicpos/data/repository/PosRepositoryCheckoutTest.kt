package id.berkahtigaputeri.basicpos.data.repository

import android.database.sqlite.SQLiteException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase
import id.berkahtigaputeri.basicpos.data.local.entity.ProductEntity
import id.berkahtigaputeri.basicpos.data.local.entity.StoreProfileEntity
import id.berkahtigaputeri.basicpos.domain.model.CheckoutLine
import id.berkahtigaputeri.basicpos.domain.model.CheckoutRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PosRepositoryCheckoutTest {
    private lateinit var database: BerkahPosDatabase
    private lateinit var repository: PosRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, BerkahPosDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PosRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun checkoutAggregatesDuplicateLinesAndKeepsHistoricalReceiptSnapshot() = runBlocking {
        val dao = database.posDao()
        dao.upsertStoreProfile(
            StoreProfileEntity(
                id = 1,
                storeName = "Toko Awal",
                ownerName = "Pemilik Awal",
                address = "Alamat Awal",
                phoneNumber = "081234567890",
                receiptFooter = "Terima kasih",
                updatedAtEpochMillis = 1
            )
        )
        val product = insertProduct("Kopi", "KOPI-01", 12_500, 10)

        val receipt = repository.checkout(
            CheckoutRequest(
                lines = listOf(
                    CheckoutLine(productId = product.id, quantity = 1),
                    CheckoutLine(productId = product.id, quantity = 2)
                ),
                discountRupiah = 2_500,
                taxRupiah = 1_000,
                paidRupiah = 40_000
            )
        )

        assertEquals(37_500L, receipt.sale.subtotalRupiah)
        assertEquals(36_000L, receipt.sale.totalRupiah)
        assertEquals(4_000L, receipt.sale.changeRupiah)
        assertEquals(7L, dao.getProductById(product.id)?.stockQuantity)
        assertEquals(1L, dao.observeTransactionCount().first())
        assertEquals(1, receipt.items.size)
        assertEquals(3L, receipt.items.single().quantity)

        dao.upsertStoreProfile(
            StoreProfileEntity(
                id = 1,
                storeName = "Toko Baru",
                ownerName = "Pemilik Baru",
                address = "Alamat Baru",
                phoneNumber = "089999999999",
                receiptFooter = "Footer baru",
                updatedAtEpochMillis = 2
            )
        )

        val historicalReceipt = repository.getTransactionReceipt(receipt.sale.id)
        assertNotNull(historicalReceipt)
        assertEquals("Toko Awal", historicalReceipt?.storeProfileSnapshot?.storeName)
        assertEquals("Pemilik Awal", historicalReceipt?.storeProfileSnapshot?.ownerName)
        assertEquals("Alamat Awal", historicalReceipt?.storeProfileSnapshot?.address)
        assertEquals("081234567890", historicalReceipt?.storeProfileSnapshot?.phoneNumber)
        assertEquals("Terima kasih", historicalReceipt?.storeProfileSnapshot?.receiptFooter)
        assertEquals("Kopi", historicalReceipt?.items?.single()?.productName)
        assertEquals("KOPI-01", historicalReceipt?.items?.single()?.sku)
        assertEquals(3L, historicalReceipt?.items?.single()?.quantity)
        assertEquals(37_500L, historicalReceipt?.items?.single()?.lineTotalRupiah)
    }

    @Test
    fun failedStockUpdateRollsBackEarlierDecrementsAndDoesNotCreateReceipt() = runBlocking {
        val dao = database.posDao()
        val first = insertProduct("Produk pertama", "FIRST", 1_000, 5)
        val second = insertProduct("Produk kedua", "SECOND", 2_000, 5)

        // Simulate a concurrent stock change after checkout has read both products but before
        // the second conditional decrement. RAISE(IGNORE) makes that UPDATE affect zero rows.
        database.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_second_stock_decrement
            BEFORE UPDATE OF stockQuantity ON products
            WHEN OLD.id = ${second.id}
            BEGIN
                SELECT RAISE(IGNORE);
            END
            """.trimIndent()
        )

        var failure: IllegalStateException? = null
        try {
            repository.checkout(
                CheckoutRequest(
                    lines = listOf(
                        CheckoutLine(productId = first.id, quantity = 1),
                        CheckoutLine(productId = second.id, quantity = 1)
                    ),
                    paidRupiah = 3_000
                )
            )
        } catch (error: IllegalStateException) {
            failure = error
        }

        assertNotNull("The second conditional stock update must fail.", failure)
        assertEquals(5L, dao.getProductById(first.id)?.stockQuantity)
        assertEquals(5L, dao.getProductById(second.id)?.stockQuantity)
        assertEquals(0L, dao.observeTransactionCount().first())
    }

    @Test
    fun receiptItemInsertFailureRollsBackStockAndTransactionHeader() = runBlocking {
        val dao = database.posDao()
        val product = insertProduct("Produk rollback", "ROLLBACK-01", 2_500, 4)

        // The header and stock decrement happen before receipt-item insertion. A trigger forces
        // that final DAO call to fail so the Room transaction must roll back every prior write.
        database.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER fail_transaction_item_insert
            BEFORE INSERT ON transaction_items
            BEGIN
                SELECT RAISE(ABORT, 'simulated receipt item insertion failure');
            END
            """.trimIndent()
        )

        var failure: SQLiteException? = null
        try {
            repository.checkout(
                CheckoutRequest(
                    lines = listOf(CheckoutLine(productId = product.id, quantity = 2)),
                    paidRupiah = 5_000
                )
            )
        } catch (error: SQLiteException) {
            failure = error
        }

        assertNotNull("Receipt-item insertion must fail.", failure)
        assertEquals(4L, dao.getProductById(product.id)?.stockQuantity)
        assertEquals(0L, dao.observeTransactionCount().first())
        val itemCount = database.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM transaction_items"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getLong(0)
        }
        assertEquals(0L, itemCount)
    }

    @Test
    fun deletingProductPreservesHistoricalReceiptItemSnapshot() = runBlocking {
        val dao = database.posDao()
        val product = insertProduct("Produk historis", "HIST-01", 3_000, 2)
        val receipt = repository.checkout(
            CheckoutRequest(
                lines = listOf(CheckoutLine(productId = product.id, quantity = 2)),
                paidRupiah = 6_000
            )
        )

        repository.deleteProduct(product.id)

        assertEquals(null, dao.getProductById(product.id))
        val historicalReceipt = repository.getTransactionReceipt(receipt.sale.id)
        assertNotNull(historicalReceipt)
        val historicalItem = historicalReceipt!!.items.single()
        assertEquals(product.id, historicalItem.productId)
        assertEquals("Produk historis", historicalItem.productName)
        assertEquals("HIST-01", historicalItem.sku)
        assertEquals(3_000L, historicalItem.unitPriceRupiah)
        assertEquals(2L, historicalItem.quantity)
        assertEquals(6_000L, historicalItem.lineTotalRupiah)
    }

    @Test
    fun concurrentCheckoutsCannotSellTheLastUnitTwice() = runBlocking {
        val dao = database.posDao()
        val product = insertProduct("Produk terbatas", "LIMITED-01", 1_500, 1)
        val request = CheckoutRequest(
            lines = listOf(CheckoutLine(productId = product.id, quantity = 1)),
            paidRupiah = 1_500
        )

        // Execute from separate worker threads so both callers contend for the same Room database.
        val outcomes = listOf(
            async(Dispatchers.IO) { runCatching { repository.checkout(request) } },
            async(Dispatchers.IO) { runCatching { repository.checkout(request) } }
        ).awaitAll()

        assertEquals(1, outcomes.count { it.isSuccess })
        assertEquals(1, outcomes.count { it.isFailure })
        assertTrue(
            outcomes.single { it.isFailure }.exceptionOrNull() is IllegalArgumentException ||
                outcomes.single { it.isFailure }.exceptionOrNull() is IllegalStateException
        )
        assertEquals(0L, dao.getProductById(product.id)?.stockQuantity)
        assertEquals(1L, dao.observeTransactionCount().first())
    }

    private suspend fun insertProduct(
        name: String,
        sku: String,
        priceRupiah: Long,
        stockQuantity: Long
    ): ProductEntity {
        database.posDao().upsertProduct(
            ProductEntity(
                name = name,
                sku = sku,
                priceRupiah = priceRupiah,
                stockQuantity = stockQuantity,
                updatedAtEpochMillis = 1
            )
        )
        return database.posDao().observeProducts("").first().single { it.name == name }
    }
}
