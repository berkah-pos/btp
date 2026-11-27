package id.berkahtigaputeri.basicpos.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the real Room migration against SQLite databases with the historical v1 layouts.
 *
 * These tests deliberately create the old schema with SQL instead of MigrationTestHelper.
 * Room 2.8.x's schema-bundle deserializer can hit a Kotlin Serialization binary-linkage
 * AbstractMethodError in the instrumentation runtime before the migration is invoked.
 * Opening the fixture through Room still executes MIGRATION_1_2 and Room's own v2 schema
 * validation, without requiring schema-bundle JSON deserialization.
 */
@RunWith(AndroidJUnit4::class)
class BerkahPosDatabaseMigrationTest {

    @Test
    fun migratesOriginalKspVersionOneAndPreservesSalesAndReceiptItems() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = ORIGINAL_V1_DATABASE
        createLegacyDatabase(context, databaseName)

        val room = openMigratedDatabase(context, databaseName)
        try {
            val migrated = room.openHelper.writableDatabase

            val sale = migrated.query(
                "SELECT transactionNumber, totalRupiah, storeNameSnapshot " +
                    "FROM sales_transactions WHERE id = 30"
            )
            sale.use {
                assertTrue(it.moveToFirst())
                assertEquals("BTP-OLD-001", it.getString(0))
                assertEquals(25_000L, it.getLong(1))
                assertTrue(
                    "Original v1 has no store snapshot; migration must preserve that fact.",
                    it.isNull(2)
                )
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
            assertTrue(
                "Migration must add productId to transaction_items.",
                "productId" in tableColumns(migrated, "transaction_items")
            )
            assertTrue(
                "Migration must create the index expected by the v2 Room schema.",
                "index_transaction_items_productId" in indexNames(migrated, "transaction_items")
            )
        } finally {
            room.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun migratesHistoricalVersionOneWithProductIdAndReceiptSnapshotsAlreadyPresent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = INTERMEDIATE_V1_DATABASE
        createLegacyDatabase(
            context = context,
            databaseName = databaseName,
            includeHistoricalProductId = true,
            includeHistoricalReceiptSnapshots = true
        )

        val room = openMigratedDatabase(context, databaseName)
        try {
            val migrated = room.openHelper.writableDatabase

            val sale = migrated.query(
                "SELECT storeNameSnapshot, storeOwnerSnapshot, storeAddressSnapshot, " +
                    "storePhoneSnapshot, receiptFooterSnapshot " +
                    "FROM sales_transactions WHERE id = 30"
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
        } finally {
            room.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun openMigratedDatabase(context: Context, databaseName: String): BerkahPosDatabase =
        Room.databaseBuilder(context, BerkahPosDatabase::class.java, databaseName)
            .addMigrations(BerkahPosDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

    /**
     * Creates the original v1 KSP schema from app/schemas/.../1.json, then optionally applies
     * the two unversioned historical ALTER TABLE changes found in repository history.
     */
    private fun createLegacyDatabase(
        context: Context,
        databaseName: String,
        includeHistoricalProductId: Boolean = false,
        includeHistoricalReceiptSnapshots: Boolean = false
    ) {
        context.deleteDatabase(databaseName)
        val database = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
        try {
            database.execSQL("PRAGMA foreign_keys = ON")

            database.execSQL(
                "CREATE TABLE store_profile (" +
                    "id INTEGER NOT NULL, storeName TEXT NOT NULL, ownerName TEXT, " +
                    "address TEXT, phoneNumber TEXT, receiptFooter TEXT, " +
                    "updatedAtEpochMillis INTEGER NOT NULL, PRIMARY KEY(id))"
            )
            database.execSQL(
                "CREATE TABLE categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, " +
                    "createdAtEpochMillis INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE UNIQUE INDEX index_categories_name ON categories(name)"
            )
            database.execSQL(
                "CREATE TABLE products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, " +
                    "sku TEXT, priceRupiah INTEGER NOT NULL, stockQuantity INTEGER NOT NULL, " +
                    "categoryId INTEGER, updatedAtEpochMillis INTEGER NOT NULL, " +
                    "FOREIGN KEY(categoryId) REFERENCES categories(id) " +
                    "ON UPDATE NO ACTION ON DELETE SET NULL)"
            )
            database.execSQL("CREATE INDEX index_products_categoryId ON products(categoryId)")
            database.execSQL("CREATE UNIQUE INDEX index_products_sku ON products(sku)")
            database.execSQL(
                "CREATE TABLE sales_transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "transactionNumber TEXT NOT NULL, createdAtEpochMillis INTEGER NOT NULL, " +
                    "subtotalRupiah INTEGER NOT NULL, discountRupiah INTEGER NOT NULL, " +
                    "taxRupiah INTEGER NOT NULL, totalRupiah INTEGER NOT NULL, " +
                    "paidRupiah INTEGER NOT NULL, changeRupiah INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE UNIQUE INDEX index_sales_transactions_transactionNumber " +
                    "ON sales_transactions(transactionNumber)"
            )
            database.execSQL(
                "CREATE INDEX index_sales_transactions_createdAtEpochMillis " +
                    "ON sales_transactions(createdAtEpochMillis)"
            )
            database.execSQL(
                "CREATE TABLE transaction_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, transactionId INTEGER NOT NULL, " +
                    "productNameSnapshot TEXT NOT NULL, skuSnapshot TEXT, " +
                    "unitPriceRupiah INTEGER NOT NULL, quantity INTEGER NOT NULL, " +
                    "lineTotalRupiah INTEGER NOT NULL, " +
                    "FOREIGN KEY(transactionId) REFERENCES sales_transactions(id) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
            )
            database.execSQL(
                "CREATE INDEX index_transaction_items_transactionId " +
                    "ON transaction_items(transactionId)"
            )

            database.execSQL(
                "CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)"
            )
            database.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                    "VALUES(42, 'c8e40701ea050d4313b70f425aa63fc9')"
            )

            insertLegacyBusinessData(database)

            if (includeHistoricalProductId) {
                // Historical commit 387603c added productId and its index without a version bump.
                database.execSQL("ALTER TABLE transaction_items ADD COLUMN productId INTEGER")
                database.execSQL(
                    "CREATE INDEX index_transaction_items_productId " +
                        "ON transaction_items(productId)"
                )
                database.execSQL(
                    "UPDATE transaction_items SET productId = 20 WHERE id = 40"
                )
            }

            if (includeHistoricalReceiptSnapshots) {
                // Historical commit 67f46d3 added these nullable columns, also at version 1.
                database.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN storeNameSnapshot TEXT"
                )
                database.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN storeOwnerSnapshot TEXT"
                )
                database.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN storeAddressSnapshot TEXT"
                )
                database.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN storePhoneSnapshot TEXT"
                )
                database.execSQL(
                    "ALTER TABLE sales_transactions ADD COLUMN receiptFooterSnapshot TEXT"
                )
                database.execSQL(
                    "UPDATE sales_transactions SET storeNameSnapshot = 'Toko Snapshot Lama', " +
                        "storeOwnerSnapshot = 'Pemilik Lama', storeAddressSnapshot = 'Alamat Lama', " +
                        "storePhoneSnapshot = '081234567890', receiptFooterSnapshot = 'Terima kasih' " +
                        "WHERE id = 30"
                )
            }

            database.version = 1
        } finally {
            database.close()
        }
    }

    private fun insertLegacyBusinessData(database: SQLiteDatabase) {
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
        database: SupportSQLiteDatabase,
        tableName: String
    ): Set<String> = database.query("PRAGMA table_info(`$tableName`)").use { cursor ->
        buildSet {
            val columnIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) add(cursor.getString(columnIndex))
        }
    }

    private fun indexNames(
        database: SupportSQLiteDatabase,
        tableName: String
    ): Set<String> = database.query("PRAGMA index_list(`$tableName`)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) add(cursor.getString(nameIndex))
        }
    }

    private fun scalarLong(
        database: SupportSQLiteDatabase,
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
