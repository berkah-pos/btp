package id.berkahtigaputeri.basicpos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = true
)
abstract class BerkahPosDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao
}
