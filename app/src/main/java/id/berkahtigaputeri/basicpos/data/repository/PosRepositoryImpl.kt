package id.berkahtigaputeri.basicpos.data.repository

import androidx.room.withTransaction
import id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase
import id.berkahtigaputeri.basicpos.data.local.entity.CategoryEntity
import id.berkahtigaputeri.basicpos.data.local.entity.ProductEntity
import id.berkahtigaputeri.basicpos.data.local.entity.SalesTransactionEntity
import id.berkahtigaputeri.basicpos.data.local.entity.StoreProfileEntity
import id.berkahtigaputeri.basicpos.data.local.entity.TransactionItemEntity
import id.berkahtigaputeri.basicpos.domain.model.Category
import id.berkahtigaputeri.basicpos.domain.model.CheckoutRequest
import id.berkahtigaputeri.basicpos.domain.model.DashboardSummary
import id.berkahtigaputeri.basicpos.domain.model.Product
import id.berkahtigaputeri.basicpos.domain.model.SaleItem
import id.berkahtigaputeri.basicpos.domain.model.SaleReceipt
import id.berkahtigaputeri.basicpos.domain.model.SaleSummary
import id.berkahtigaputeri.basicpos.domain.model.StoreProfile
import id.berkahtigaputeri.basicpos.domain.repository.PosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class PosRepositoryImpl(
    private val database: BerkahPosDatabase
) : PosRepository {
    private val dao = database.posDao()

    override fun observeProducts(query: String): Flow<List<Product>> =
        dao.observeProducts(query.trim()).map { rows -> rows.map(::toDomain) }

    override fun observeCategories(): Flow<List<Category>> =
        dao.observeCategories().map { rows ->
            rows.map { Category(id = it.id, name = it.name) }
        }

    override fun observeStoreProfile(): Flow<StoreProfile?> =
        dao.observeStoreProfile().map { row -> row?.let(::toDomain) }

    override fun observeRecentTransactions(limit: Int): Flow<List<SaleSummary>> =
        dao.observeRecentTransactions(limit.coerceIn(1, 100)).map { rows -> rows.map(::toDomain) }

    override fun observeLowStockProducts(threshold: Long, limit: Int): Flow<List<Product>> =
        dao.observeLowStockProducts(threshold.coerceAtLeast(0), limit.coerceIn(1, 100))
            .map { rows -> rows.map(::toDomain) }

    override fun observeDashboardSummary(): Flow<DashboardSummary> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val end = calendar.timeInMillis

        return combine(
            dao.observeSalesBetween(start, end),
            dao.observeTransactionCountBetween(start, end),
            dao.observeProductCount(),
            dao.observeLowStockProducts(threshold = DEFAULT_LOW_STOCK_THRESHOLD, limit = 5),
            dao.observeRecentTransactions(limit = 5)
        ) { sales, transactions, products, lowStock, recent ->
            DashboardSummary(
                totalSalesTodayRupiah = sales,
                transactionCountToday = transactions,
                productCount = products,
                lowStockProducts = lowStock.map(::toDomain),
                recentTransactions = recent.map(::toDomain)
            )
        }
    }

    override suspend fun saveProduct(product: Product) {
        require(product.name.isNotBlank()) { "Nama produk wajib diisi." }
        require(product.priceRupiah >= 0) { "Harga tidak boleh negatif." }
        require(product.stockQuantity >= 0) { "Stok tidak boleh negatif." }
        val normalizedSku = product.sku?.trim()?.takeIf(String::isNotEmpty)
        dao.upsertProduct(
            ProductEntity(
                id = product.id,
                name = product.name.trim(),
                sku = normalizedSku,
                priceRupiah = product.priceRupiah,
                stockQuantity = product.stockQuantity,
                categoryId = product.categoryId,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteProduct(productId: Long) {
        val existing = dao.getProductById(productId) ?: return
        dao.deleteProduct(existing)
    }

    override suspend fun saveCategory(category: Category) {
        val name = category.name.trim()
        require(name.isNotEmpty()) { "Nama kategori wajib diisi." }
        dao.upsertCategory(
            CategoryEntity(
                id = category.id,
                name = name,
                createdAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun saveStoreProfile(profile: StoreProfile) {
        val storeName = profile.storeName.trim()
        require(storeName.isNotEmpty()) { "Nama toko wajib diisi." }
        dao.upsertStoreProfile(
            StoreProfileEntity(
                id = 1,
                storeName = storeName,
                ownerName = profile.ownerName?.trim()?.takeIf(String::isNotEmpty),
                address = profile.address?.trim()?.takeIf(String::isNotEmpty),
                phoneNumber = profile.phoneNumber?.trim()?.takeIf(String::isNotEmpty),
                receiptFooter = profile.receiptFooter?.trim()?.takeIf(String::isNotEmpty),
                updatedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun checkout(request: CheckoutRequest): SaleReceipt {
        require(request.lines.isNotEmpty()) { "Keranjang masih kosong." }
        require(request.discountRupiah >= 0) { "Diskon tidak boleh negatif." }
        require(request.taxRupiah >= 0) { "Pajak tidak boleh negatif." }
        require(request.paidRupiah >= 0) { "Pembayaran tidak boleh negatif." }

        return database.withTransaction {
            val distinctLines = request.lines.groupBy { it.productId }
                .map { (productId, lines) ->
                    val quantity = lines.fold(0L) { total, line ->
                        require(line.quantity > 0) { "Jumlah item harus lebih dari nol." }
                        Math.addExact(total, line.quantity)
                    }
                    productId to quantity
                }

            val products = distinctLines.map { (productId, quantity) ->
                val product = dao.getProductById(productId)
                    ?: throw IllegalArgumentException("Produk sudah tidak tersedia.")
                require(product.stockQuantity >= quantity) {
                    "Stok ${product.name} tidak mencukupi."
                }
                product to quantity
            }

            val saleItems = products.map { (product, quantity) ->
                val lineTotal = Math.multiplyExact(product.priceRupiah, quantity)
                TransactionItemEntity(
                    transactionId = 0,
                    productId = product.id,
                    productNameSnapshot = product.name,
                    skuSnapshot = product.sku,
                    unitPriceRupiah = product.priceRupiah,
                    quantity = quantity,
                    lineTotalRupiah = lineTotal
                )
            }

            val subtotal = saleItems.fold(0L) { total, item ->
                Math.addExact(total, item.lineTotalRupiah)
            }
            require(request.discountRupiah <= subtotal) {
                "Diskon tidak boleh melebihi subtotal."
            }
            val total = Math.addExact(
                Math.subtractExact(subtotal, request.discountRupiah),
                request.taxRupiah
            )
            require(request.paidRupiah >= total) {
                "Uang pembayaran masih kurang."
            }
            val change = Math.subtractExact(request.paidRupiah, total)
            val createdAt = System.currentTimeMillis()
            val transactionNumber = createTransactionNumber(createdAt)

            // Stock updates, sale header, and receipt lines commit atomically.
            products.forEach { (product, quantity) ->
                val updated = dao.decrementStockIfAvailable(product.id, quantity)
                check(updated == 1) { "Stok ${product.name} berubah. Silakan ulangi checkout." }
            }

            val transactionId = dao.insertTransaction(
                SalesTransactionEntity(
                    transactionNumber = transactionNumber,
                    createdAtEpochMillis = createdAt,
                    subtotalRupiah = subtotal,
                    discountRupiah = request.discountRupiah,
                    taxRupiah = request.taxRupiah,
                    totalRupiah = total,
                    paidRupiah = request.paidRupiah,
                    changeRupiah = change
                )
            )
            val savedItems = saleItems.map { it.copy(transactionId = transactionId) }
            dao.insertTransactionItems(savedItems)

            SaleReceipt(
                sale = SaleSummary(
                    id = transactionId,
                    transactionNumber = transactionNumber,
                    createdAtEpochMillis = createdAt,
                    totalRupiah = total,
                    paidRupiah = request.paidRupiah,
                    changeRupiah = change
                ),
                subtotalRupiah = subtotal,
                discountRupiah = request.discountRupiah,
                taxRupiah = request.taxRupiah,
                items = savedItems.map(::toDomain)
            )
        }
    }

    override suspend fun getTransactionItems(transactionId: Long): List<SaleItem> =
        dao.getTransactionItems(transactionId).map(::toDomain)

    private fun toDomain(row: ProductEntity) = Product(
        id = row.id,
        name = row.name,
        sku = row.sku,
        priceRupiah = row.priceRupiah,
        stockQuantity = row.stockQuantity,
        categoryId = row.categoryId
    )

    private fun toDomain(row: StoreProfileEntity) = StoreProfile(
        storeName = row.storeName,
        ownerName = row.ownerName,
        address = row.address,
        phoneNumber = row.phoneNumber,
        receiptFooter = row.receiptFooter
    )

    private fun toDomain(row: SalesTransactionEntity) = SaleSummary(
        id = row.id,
        transactionNumber = row.transactionNumber,
        createdAtEpochMillis = row.createdAtEpochMillis,
        totalRupiah = row.totalRupiah,
        paidRupiah = row.paidRupiah,
        changeRupiah = row.changeRupiah
    )

    private fun toDomain(row: TransactionItemEntity) = SaleItem(
        productId = row.productId,
        productName = row.productNameSnapshot,
        sku = row.skuSnapshot,
        unitPriceRupiah = row.unitPriceRupiah,
        quantity = row.quantity,
        lineTotalRupiah = row.lineTotalRupiah
    )

    private fun createTransactionNumber(epochMillis: Long): String {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(Date(epochMillis))
        return "BTP-$stamp-${UUID.randomUUID().toString().take(8).uppercase(Locale.ROOT)}"
    }

    companion object {
        const val DEFAULT_LOW_STOCK_THRESHOLD = 5L
    }
}
