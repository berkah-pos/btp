package id.berkahtigaputeri.basicpos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import id.berkahtigaputeri.basicpos.data.local.dao.PosDao
import id.berkahtigaputeri.basicpos.data.local.entity.CategoryEntity
import id.berkahtigaputeri.basicpos.data.local.entity.ProductEntity
import id.berkahtigaputeri.basicpos.data.local.entity.SalesTransactionEntity
import id.berkahtigaputeri.basicpos.data.local.entity.StoreProfileEntity
import id.berkahtigaputeri.basicpos.data.local.entity.TransactionItemEntity

@Database(
    entities = [
        StoreProfileEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        SalesTransactionEntity::class,
        TransactionItemEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class BerkahPosDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        /**
         * Room version 1 was changed without a version bump twice in the original Git history:
         * transaction_items.productId was added first, then five receipt snapshot columns.
         * Some installed version-1 databases therefore already contain either or both changes.
         *
         * Inspect the actual SQLite schema and add only missing nullable columns so both the
         * original KSP v1 schema and the historical intermediate v1 shapes migrate safely.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.ensureColumn("transaction_items", "productId", "INTEGER")
                database.ensureColumn("sales_transactions", "storeNameSnapshot", "TEXT")
                database.ensureColumn("sales_transactions", "storeOwnerSnapshot", "TEXT")
                database.ensureColumn("sales_transactions", "storeAddressSnapshot", "TEXT")
                database.ensureColumn("sales_transactions", "storePhoneSnapshot", "TEXT")
                database.ensureColumn("sales_transactions", "receiptFooterSnapshot", "TEXT")

                if ("index_transaction_items_productId" !in database.indexNames("transaction_items")) {
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_transaction_items_productId` " +
                            "ON `transaction_items` (`productId`)"
                    )
                }
            }
        }

        private fun SupportSQLiteDatabase.ensureColumn(
            tableName: String,
            columnName: String,
            columnType: String
        ) {
            if (columnName !in columnNames(tableName)) {
                execSQL(
                    "ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnType"
                )
            }
        }

        private fun SupportSQLiteDatabase.columnNames(tableName: String): Set<String> =
            query("PRAGMA table_info(`$tableName`)").use { cursor ->
                buildSet {
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        add(cursor.getString(nameIndex))
                    }
                }
            }

        private fun SupportSQLiteDatabase.indexNames(tableName: String): Set<String> =
            query("PRAGMA index_list(`$tableName`)").use { cursor ->
                buildSet {
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) {
                        add(cursor.getString(nameIndex))
                    }
                }
            }
    }
}
