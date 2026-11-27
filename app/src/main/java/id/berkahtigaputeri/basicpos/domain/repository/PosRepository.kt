package id.berkahtigaputeri.basicpos.domain.repository

import id.berkahtigaputeri.basicpos.domain.model.Category
import id.berkahtigaputeri.basicpos.domain.model.CheckoutRequest
import id.berkahtigaputeri.basicpos.domain.model.DashboardSummary
import id.berkahtigaputeri.basicpos.domain.model.Product
import id.berkahtigaputeri.basicpos.domain.model.SaleItem
import id.berkahtigaputeri.basicpos.domain.model.SaleReceipt
import id.berkahtigaputeri.basicpos.domain.model.SaleSummary
import id.berkahtigaputeri.basicpos.domain.model.StoreProfile
import kotlinx.coroutines.flow.Flow

interface PosRepository {
    fun observeDashboardSummary(lowStockThreshold: Long = 5L): Flow<DashboardSummary>
    fun observeProducts(query: String = ""): Flow<List<Product>>
    fun observeCategories(): Flow<List<Category>>
    fun observeStoreProfile(): Flow<StoreProfile?>
    fun observeRecentTransactions(limit: Int = 20): Flow<List<SaleSummary>>
    fun observeTransactions(query: String = ""): Flow<List<SaleSummary>>
    fun observeLowStockProducts(threshold: Long = 5, limit: Int = 20): Flow<List<Product>>

    suspend fun saveProduct(product: Product)
    suspend fun seedSampleProducts()
    suspend fun deleteProduct(productId: Long)
    suspend fun saveCategory(category: Category)
    suspend fun saveStoreProfile(profile: StoreProfile)
    suspend fun checkout(request: CheckoutRequest): SaleReceipt
    suspend fun getTransactionItems(transactionId: Long): List<SaleItem>
    suspend fun getTransactionReceipt(transactionId: Long): SaleReceipt?
}
