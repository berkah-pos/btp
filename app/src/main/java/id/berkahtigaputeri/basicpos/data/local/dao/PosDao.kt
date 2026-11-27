package id.berkahtigaputeri.basicpos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
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

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCountOnce(): Long

    @Insert
    suspend fun insertSampleProducts(rows: List<ProductEntity>)

    @Query("SELECT COUNT(*) FROM sales_transactions")
    fun observeTransactionCount(): Flow<Long>

    @Query(
        """SELECT * FROM products
        WHERE :query = ''
           OR name LIKE '%' || :query || '%' COLLATE NOCASE
           OR COALESCE(sku, '') LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name COLLATE NOCASE ASC"""
    )
    fun observeProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products WHERE stockQuantity <= :threshold")
    fun observeLowStockCount(threshold: Long): Flow<Long>

    @Query("SELECT * FROM products WHERE stockQuantity <= :threshold ORDER BY stockQuantity ASC, name ASC LIMIT :limit")
    fun observeLowStockProducts(threshold: Long, limit: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    fun observeStoreProfile(): Flow<StoreProfileEntity?>

    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    suspend fun getStoreProfileForBackup(): StoreProfileEntity?

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllCategoriesForBackup(): List<CategoryEntity>

    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllProductsForBackup(): List<ProductEntity>

    @Query("SELECT * FROM sales_transactions ORDER BY id ASC")
    suspend fun getAllTransactionsForBackup(): List<SalesTransactionEntity>

    @Query("SELECT * FROM transaction_items ORDER BY id ASC")
    suspend fun getAllTransactionItemsForBackup(): List<TransactionItemEntity>

    @Query("DELETE FROM transaction_items")
    suspend fun deleteAllTransactionItems()

    @Query("DELETE FROM sales_transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM store_profile")
    suspend fun deleteAllStoreProfiles()

    @Insert
    suspend fun insertCategoriesForBackup(rows: List<CategoryEntity>)

    @Insert
    suspend fun insertProductsForBackup(rows: List<ProductEntity>)

    @Insert
    suspend fun insertTransactionsForBackup(rows: List<SalesTransactionEntity>)

    @Insert
    suspend fun insertTransactionItemsForBackup(rows: List<TransactionItemEntity>)

    @Upsert
    suspend fun upsertStoreProfile(profile: StoreProfileEntity)

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)

    @Upsert
    suspend fun upsertProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM sales_transactions ORDER BY createdAtEpochMillis DESC LIMIT :limit")
    fun observeRecentTransactions(limit: Int): Flow<List<SalesTransactionEntity>>

    @Query("""SELECT * FROM sales_transactions
        WHERE :query = '' OR transactionNumber LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY createdAtEpochMillis DESC""")
    fun observeTransactions(query: String): Flow<List<SalesTransactionEntity>>

    @Query("SELECT * FROM sales_transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Long): SalesTransactionEntity?

    @Query("SELECT COALESCE(SUM(totalRupiah), 0) FROM sales_transactions WHERE createdAtEpochMillis >= :startInclusive AND createdAtEpochMillis < :endExclusive")
    fun observeSalesBetween(startInclusive: Long, endExclusive: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM sales_transactions WHERE createdAtEpochMillis >= :startInclusive AND createdAtEpochMillis < :endExclusive")
    fun observeTransactionCountBetween(startInclusive: Long, endExclusive: Long): Flow<Long>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId ORDER BY id ASC")
    suspend fun getTransactionItems(transactionId: Long): List<TransactionItemEntity>

    @Insert
    suspend fun insertTransaction(transaction: SalesTransactionEntity): Long

    @Insert
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId AND stockQuantity >= :quantity")
    suspend fun decrementStockIfAvailable(productId: Long, quantity: Long): Int
}
