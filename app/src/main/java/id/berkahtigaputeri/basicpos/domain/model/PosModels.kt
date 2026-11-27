package id.berkahtigaputeri.basicpos.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val sku: String? = null,
    val priceRupiah: Long,
    val stockQuantity: Long,
    val categoryId: Long? = null
)

data class Category(
    val id: Long = 0,
    val name: String
)

data class StoreProfile(
    val storeName: String,
    val ownerName: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val receiptFooter: String? = null
)

data class SaleSummary(
    val id: Long,
    val transactionNumber: String,
    val createdAtEpochMillis: Long,
    val subtotalRupiah: Long,
    val discountRupiah: Long,
    val taxRupiah: Long,
    val totalRupiah: Long,
    val paidRupiah: Long,
    val changeRupiah: Long
)

data class SaleItem(
    val productId: Long?,
    val productName: String,
    val sku: String?,
    val unitPriceRupiah: Long,
    val quantity: Long,
    val lineTotalRupiah: Long
)

data class CheckoutLine(
    val productId: Long,
    val quantity: Long
)

data class CheckoutRequest(
    val lines: List<CheckoutLine>,
    val discountRupiah: Long = 0,
    val taxRupiah: Long = 0,
    val paidRupiah: Long
)

data class SaleReceipt(
    val sale: SaleSummary,
    val subtotalRupiah: Long,
    val discountRupiah: Long,
    val taxRupiah: Long,
    val items: List<SaleItem>
)

data class DashboardSummary(
    val totalSalesTodayRupiah: Long,
    val transactionCountToday: Long,
    val productCount: Long,
    val lowStockCount: Long,
    val lowStockProducts: List<Product>,
    val recentTransactions: List<SaleSummary>
)
