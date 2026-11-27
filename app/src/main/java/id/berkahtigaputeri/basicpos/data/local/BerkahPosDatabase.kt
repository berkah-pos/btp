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
         * Preserve existing sales when adding immutable store details to historical receipts.
         * All new columns are nullable so pre-existing transactions remain valid.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
            }
        }
    }
}
