package id.berkahtigaputeri.basicpos.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase
import id.berkahtigaputeri.basicpos.data.local.entity.ProductEntity
import id.berkahtigaputeri.basicpos.data.local.entity.StoreProfileEntity
import id.berkahtigaputeri.basicpos.domain.model.CheckoutLine
import id.berkahtigaputeri.basicpos.domain.model.CheckoutRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
