package id.berkahtigaputeri.basicpos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import id.berkahtigaputeri.basicpos.data.local.entity.CategoryEntity
import id.berkahtigaputeri.basicpos.data.local.entity.ProductEntity
import id.berkahtigaputeri.basicpos.data.local.entity.SalesTransactionEntity
import id.berkahtigaputeri.basicpos.data.local.entity.StoreProfileEntity
import id.berkahtigaputeri.basicpos.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    @Query("SELECT COUNT(*) FROM products")
    fun observeProductCount(): Flow<Long>

    @Query("SELECT COUNT(*) FROM sales_transactions")
    fun observeTransactionCount(): Flow<Long>

    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    fun observeStoreProfile(): Flow<StoreProfileEntity?>

    @Upsert
    suspend fun upsertStoreProfile(profile: StoreProfileEntity)

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)

    @Upsert
    suspend fun upsertProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Upsert
    suspend fun upsertTransaction(transaction: SalesTransactionEntity)

    @Upsert
    suspend fun upsertTransactionItems(items: List<TransactionItemEntity>)
}
