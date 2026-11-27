package id.berkahtigaputeri.basicpos.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BerkahPosDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BerkahPosDatabase::class.java
    )

    @Test
    fun migratesOriginalKspVersionOneAndPreservesSalesAndReceiptItems() {
        val legacy = helper.createDatabase(ORIGINAL_V1_DATABASE, 1)
        insertLegacyBusinessData(legacy)
        legacy.close()

        val migrated = helper.runMigrationsAndValidate(
            ORIGINAL_V1_DATABASE,
            2,
            true,
            BerkahPosDatabase.MIGRATION_1_2
        )

        val sale = migrated.query(
            "SELECT transactionNumber, totalRupiah, storeNameSnapshot " +
                "FROM sales_transactions WHERE id = 30"
        )
        sale.use {
            assertTrue(it.moveToFirst())
            assertEquals("BTP-OLD-001", it.getString(0))
            assertEquals(25_000L, it.getLong(1))
            assertTrue("Original v1 has no store snapshot; migration must preserve that fact.", it.isNull(2))
        }

        val item = migrated.query(
            "SELECT productId, productNameSnapshot, skuSnapshot, quantity, lineTotalRupiah " +
                "FROM transaction_items WHERE id = 40"
        )
        item.use {
            assertTrue(it.moveToFirst())
            assertTrue("Original v1 transaction items had no productId column.", it.isNull(0))
            assertEquals("Kopi", it.getString(1))
            assertEquals("KOPI-01", it.getString(2))
            assertEquals(2L, it.getLong(3))
            assertEquals(25_000L, it.getLong(4))
        }

        assertEquals(
            setOf(
                "storeNameSnapshot",
                "storeOwnerSnapshot",
                "storeAddressSnapshot",
                "storePhoneSnapshot",
                "receiptFooterSnapshot"
            ),
            tableColumns(migrated, "sales_transactions").intersect(
                setOf(
                    "storeNameSnapshot",
                    "storeOwnerSnapshot",
                    "storeAddressSnapshot",
                    "storePhoneSnapshot",
                    "receiptFooterSnapshot"
                )
            )
        )
        assertTrue("Migration must add productId to transaction_items.", "productId" in tableColumns(migrated, "transaction_items"))
        assertTrue(
            "Migration must create the index expected by the v2 Room schema.",
            "index_transaction_items_productId" in indexNames(migrated, "transaction_items")
        )
        migrated.close()
    }

    @Test
    fun migratesHistoricalVersionOneWithProductIdAndReceiptSnapshotsAlreadyPresent() {
        val legacy = helper.createDatabase(INTERMEDIATE_V1_DATABASE, 1)
        insertLegacyBusinessData(legacy)

        // These are the actual unversioned schema changes in repository history:
        // commit 387603c added productId + its index while the database stayed at version 1;
        // commit 67f46d3 added the five nullable receipt snapshot columns, also at version 1.
        legacy.execSQL("ALTER TABLE transaction_items ADD COLUMN productId INTEGER")
        legacy.execSQL(
            "CREATE INDEX index_transaction_items_productId ON transaction_items(productId)"
        )
        legacy.execSQL("UPDATE transaction_items SET productId = 20 WHERE id = 40")

        legacy.execSQL("ALTER TABLE sales_transactions ADD COLUMN storeNameSnapshot TEXT")
        legacy.execSQL("ALTER TABLE sales_transactions ADD COLUMN storeOwnerSnapshot TEXT")
        legacy.execSQL("ALTER TABLE sales_transactions ADD COLUMN storeAddressSnapshot TEXT")
        legacy.execSQL("ALTER TABLE sales_transactions ADD COLUMN storePhoneSnapshot TEXT")
        legacy.execSQL("ALTER TABLE sales_transactions ADD COLUMN receiptFooterSnapshot TEXT")
        legacy.execSQL(
            "UPDATE sales_transactions SET storeNameSnapshot = 'Toko Snapshot Lama', " +
                "storeOwnerSnapshot = 'Pemilik Lama', storeAddressSnapshot = 'Alamat Lama', " +
                "storePhoneSnapshot = '081234567890', receiptFooterSnapshot = 'Terima kasih' " +
                "WHERE id = 30"
        )
        legacy.close()

        val migrated = helper.runMigrationsAndValidate(
            INTERMEDIATE_V1_DATABASE,
            2,
            true,
            BerkahPosDatabase.MIGRATION_1_2
        )

        val sale = migrated.query(
            "SELECT storeNameSnapshot, storeOwnerSnapshot, storeAddressSnapshot, " +
                "storePhoneSnapshot, receiptFooterSnapshot FROM sales_transactions WHERE id = 30"
        )
        sale.use {
            assertTrue(it.moveToFirst())
            assertEquals("Toko Snapshot Lama", it.getString(0))
            assertEquals("Pemilik Lama", it.getString(1))
            assertEquals("Alamat Lama", it.getString(2))
            assertEquals("081234567890", it.getString(3))
            assertEquals("Terima kasih", it.getString(4))
        }

        val item = migrated.query(
            "SELECT productId, productNameSnapshot, quantity, lineTotalRupiah " +
                "FROM transaction_items WHERE id = 40"
        )
        item.use {
            assertTrue(it.moveToFirst())
            assertEquals(20L, it.getLong(0))
            assertEquals("Kopi", it.getString(1))
            assertEquals(2L, it.getLong(2))
            assertEquals(25_000L, it.getLong(3))
        }

        assertEquals(1L, scalarLong(migrated, "SELECT COUNT(*) FROM sales_transactions"))
        assertEquals(1L, scalarLong(migrated, "SELECT COUNT(*) FROM transaction_items"))
        assertEquals(
            1L,
            scalarLong(
                migrated,
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' " +
                    "AND name = 'index_transaction_items_productId'"
            )
        )
        migrated.close()
    }

    private fun insertLegacyBusinessData(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL(
            "INSERT INTO store_profile " +
                "(id, storeName, updatedAtEpochMillis) VALUES (1, 'Toko Lama', 100)"
        )
        database.execSQL(
            "INSERT INTO categories (id, name, createdAtEpochMillis) " +
                "VALUES (10, 'Minuman', 100)"
        )
        database.execSQL(
            "INSERT INTO products " +
                "(id, name, sku, priceRupiah, stockQuantity, categoryId, updatedAtEpochMillis) " +
                "VALUES (20, 'Kopi', 'KOPI-01', 12500, 8, 10, 100)"
        )
        database.execSQL(
            "INSERT INTO sales_transactions " +
                "(id, transactionNumber, createdAtEpochMillis, subtotalRupiah, discountRupiah, " +
                "taxRupiah, totalRupiah, paidRupiah, changeRupiah) " +
                "VALUES (30, 'BTP-OLD-001', 200, 25000, 0, 0, 25000, 30000, 5000)"
        )
        database.execSQL(
            "INSERT INTO transaction_items " +
                "(id, transactionId, productNameSnapshot, skuSnapshot, unitPriceRupiah, " +
                "quantity, lineTotalRupiah) " +
                "VALUES (40, 30, 'Kopi', 'KOPI-01', 12500, 2, 25000)"
        )
    }

    private fun tableColumns(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        tableName: String
    ): Set<String> = database.query("PRAGMA table_info(`$tableName`)").use { cursor ->
        buildSet {
            val columnIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) add(cursor.getString(columnIndex))
        }
    }

    private fun indexNames(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        tableName: String
    ): Set<String> = database.query("PRAGMA index_list(`$tableName`)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) add(cursor.getString(nameIndex))
        }
    }

    private fun scalarLong(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        sql: String
    ): Long = database.query(sql).use { cursor ->
        assertTrue(cursor.moveToFirst())
        cursor.getLong(0)
    }

    private companion object {
        const val ORIGINAL_V1_DATABASE = "room-original-v1.db"
        const val INTERMEDIATE_V1_DATABASE = "room-intermediate-v1.db"
    }
}
