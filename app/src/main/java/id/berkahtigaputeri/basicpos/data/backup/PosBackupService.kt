package id.berkahtigaputeri.basicpos.data.backup

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import id.berkahtigaputeri.basicpos.data.local.BerkahPosDatabase
import id.berkahtigaputeri.basicpos.data.local.entity.CategoryEntity
import id.berkahtigaputeri.basicpos.data.local.entity.ProductEntity
import id.berkahtigaputeri.basicpos.data.local.entity.SalesTransactionEntity
import id.berkahtigaputeri.basicpos.data.local.entity.StoreProfileEntity
import id.berkahtigaputeri.basicpos.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Locale

class PosBackupService(
    private val database: BerkahPosDatabase,
    private val contentResolver: ContentResolver
) {
    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val json = database.withTransaction {
            val dao = database.posDao()
            JSONObject()
                .put("format", FORMAT_NAME)
                .put("schemaVersion", SCHEMA_VERSION)
                .put("exportedAtEpochMillis", System.currentTimeMillis())
                .put("storeProfile", dao.getStoreProfileForBackup()?.toJson() ?: JSONObject.NULL)
                .put("categories", JSONArray().apply {
                    dao.getAllCategoriesForBackup().forEach { put(it.toJson()) }
                })
                .put("products", JSONArray().apply {
                    dao.getAllProductsForBackup().forEach { put(it.toJson()) }
                })
                .put("transactions", JSONArray().apply {
                    dao.getAllTransactionsForBackup().forEach { put(it.toJson()) }
                })
                .put("transactionItems", JSONArray().apply {
                    dao.getAllTransactionItemsForBackup().forEach { put(it.toJson()) }
                })
                .toString(2)
        }

        val output = contentResolver.openOutputStream(uri, "wt")
            ?: throw IllegalStateException("File tujuan tidak dapat dibuka.")
        output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
    }

    suspend fun restoreFrom(uri: Uri): RestoreSummary = withContext(Dispatchers.IO) {
        val input = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("File cadangan tidak dapat dibuka.")
        val bytes = input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            var total = 0
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                total += count
                require(total <= MAX_BACKUP_BYTES) {
                    "Ukuran file cadangan melebihi batas 25 MB."
                }
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }

        val root = runCatching { JSONObject(String(bytes, Charsets.UTF_8)) }
            .getOrElse { throw IllegalArgumentException("File bukan JSON yang valid.", it) }
        require(root.getString("format") == FORMAT_NAME) {
            "Format file bukan cadangan BERKAH POS."
        }
        require(root.getInt("schemaVersion") == SCHEMA_VERSION) {
            "Versi cadangan tidak didukung aplikasi ini."
        }

        require(root.has("storeProfile")) { "Bagian profil toko tidak ditemukan di cadangan." }
        val rawProfile = root.get("storeProfile")
        val profile = when {
            rawProfile === JSONObject.NULL -> null
            rawProfile is JSONObject -> rawProfile.toProfile()
            else -> throw IllegalArgumentException("Profil toko pada cadangan harus berupa objek atau null.")
        }
        val categories = root.getJSONArray("categories").decode(MAX_CATEGORIES) { it.toCategory() }
        val products = root.getJSONArray("products").decode(MAX_PRODUCTS) { it.toProduct() }
        val transactions = root.getJSONArray("transactions").decode(MAX_TRANSACTIONS) { it.toTransaction() }
        val items = root.getJSONArray("transactionItems").decode(MAX_ITEMS) { it.toTransactionItem() }

        validate(profile, categories, products, transactions, items)

        // Restore is all-or-nothing: invalid inserts roll back the complete replacement.
        database.withTransaction {
            val dao = database.posDao()
            dao.deleteAllTransactionItems()
            dao.deleteAllTransactions()
            dao.deleteAllProducts()
            dao.deleteAllCategories()
            dao.deleteAllStoreProfiles()

            profile?.let { dao.upsertStoreProfile(it) }
            if (categories.isNotEmpty()) dao.insertCategoriesForBackup(categories)
            if (products.isNotEmpty()) dao.insertProductsForBackup(products)
            if (transactions.isNotEmpty()) dao.insertTransactionsForBackup(transactions)
            if (items.isNotEmpty()) dao.insertTransactionItemsForBackup(items)
        }

        RestoreSummary(categories.size, products.size, transactions.size, items.size)
    }

    private fun validate(
        profile: StoreProfileEntity?,
        categories: List<CategoryEntity>,
        products: List<ProductEntity>,
        transactions: List<SalesTransactionEntity>,
        items: List<TransactionItemEntity>
    ) {
        require(profile == null || (
            profile.id == 1 &&
                profile.storeName.isNotBlank() &&
                profile.updatedAtEpochMillis >= 0
        )) {
            "Profil toko pada file cadangan tidak valid."
        }

        uniqueIds(categories.map { it.id }, "kategori")
        uniqueIds(products.map { it.id }, "produk")
        uniqueIds(transactions.map { it.id }, "transaksi")
        uniqueIds(items.map { it.id }, "item transaksi")

        require(categories.all { it.name.isNotBlank() && it.createdAtEpochMillis >= 0 } &&
            categories.map { it.name.trim().lowercase(Locale.ROOT) }.toSet().size == categories.size
        ) { "Nama kategori kosong atau ganda." }

        val categoryIds = categories.map { it.id }.toSet()
        require(products.all {
            it.name.isNotBlank() && it.priceRupiah >= 0 && it.stockQuantity >= 0 &&
                it.updatedAtEpochMillis >= 0 &&
                (it.categoryId == null || it.categoryId in categoryIds)
        }) { "Data produk atau referensi kategori tidak valid." }

        val skus = products.mapNotNull { it.sku?.trim()?.takeIf(String::isNotEmpty) }
        require(skus.toSet().size == skus.size) { "Terdapat SKU produk ganda." }

        val transactionIds = transactions.map { it.id }.toSet()
        require(transactions.all { sale ->
            sale.transactionNumber.isNotBlank() &&
                sale.createdAtEpochMillis > 0 &&
                sale.subtotalRupiah >= 0 &&
                sale.discountRupiah in 0..sale.subtotalRupiah &&
                sale.taxRupiah >= 0 &&
                sale.totalRupiah == Math.addExact(
                    Math.subtractExact(sale.subtotalRupiah, sale.discountRupiah),
                    sale.taxRupiah
                ) &&
                sale.paidRupiah >= sale.totalRupiah &&
                sale.changeRupiah == sale.paidRupiah - sale.totalRupiah
        }) { "Nominal atau nomor transaksi pada cadangan tidak valid." }
        require(transactions.map { it.transactionNumber }.toSet().size == transactions.size) {
            "Nomor transaksi ganda pada cadangan."
        }

        require(items.all { item ->
            item.transactionId in transactionIds &&
                item.productNameSnapshot.isNotBlank() &&
                item.unitPriceRupiah >= 0 &&
                item.quantity > 0 &&
                item.lineTotalRupiah == Math.multiplyExact(item.unitPriceRupiah, item.quantity)
        }) { "Rincian item transaksi tidak valid." }

        val itemTotals = items.groupBy { it.transactionId }.mapValues { (_, rows) ->
            rows.fold(0L) { sum, row -> Math.addExact(sum, row.lineTotalRupiah) }
        }
        require(transactions.all { itemTotals[it.id] == it.subtotalRupiah }) {
            "Subtotal transaksi tidak sama dengan jumlah rincian item."
        }
    }

    private fun uniqueIds(ids: List<Long>, label: String) {
        require(ids.all { it > 0 } && ids.toSet().size == ids.size) {
            "ID $label tidak valid atau ganda."
        }
    }

    private inline fun <T> JSONArray.decode(limit: Int, convert: (JSONObject) -> T): List<T> {
        require(length() <= limit) { "Jumlah data cadangan melebihi batas." }
        return List(length()) { getJSONObject(it).let(convert) }
    }

    private fun StoreProfileEntity.toJson() = JSONObject()
        .put("id", id)
        .put("storeName", storeName)
        .put("ownerName", ownerName ?: JSONObject.NULL)
        .put("address", address ?: JSONObject.NULL)
        .put("phoneNumber", phoneNumber ?: JSONObject.NULL)
        .put("receiptFooter", receiptFooter ?: JSONObject.NULL)
        .put("updatedAtEpochMillis", updatedAtEpochMillis)

    private fun CategoryEntity.toJson() = JSONObject()
        .put("id", id).put("name", name).put("createdAtEpochMillis", createdAtEpochMillis)

    private fun ProductEntity.toJson() = JSONObject()
        .put("id", id).put("name", name).put("sku", sku ?: JSONObject.NULL)
        .put("priceRupiah", priceRupiah).put("stockQuantity", stockQuantity)
        .put("categoryId", categoryId ?: JSONObject.NULL).put("updatedAtEpochMillis", updatedAtEpochMillis)

    private fun SalesTransactionEntity.toJson() = JSONObject()
        .put("id", id).put("transactionNumber", transactionNumber)
        .put("createdAtEpochMillis", createdAtEpochMillis)
        .put("subtotalRupiah", subtotalRupiah).put("discountRupiah", discountRupiah)
        .put("taxRupiah", taxRupiah).put("totalRupiah", totalRupiah)
        .put("paidRupiah", paidRupiah).put("changeRupiah", changeRupiah)

    private fun TransactionItemEntity.toJson() = JSONObject()
        .put("id", id).put("transactionId", transactionId)
        .put("productId", productId ?: JSONObject.NULL)
        .put("productNameSnapshot", productNameSnapshot)
        .put("skuSnapshot", skuSnapshot ?: JSONObject.NULL)
        .put("unitPriceRupiah", unitPriceRupiah).put("quantity", quantity)
        .put("lineTotalRupiah", lineTotalRupiah)

    private fun JSONObject.toProfile() = StoreProfileEntity(
        id = long("id").also { require(it == 1L) }.toInt(),
        storeName = getString("storeName"),
        ownerName = optionalString("ownerName"),
        address = optionalString("address"),
        phoneNumber = optionalString("phoneNumber"),
        receiptFooter = optionalString("receiptFooter"),
        updatedAtEpochMillis = long("updatedAtEpochMillis")
    )

    private fun JSONObject.toCategory() = CategoryEntity(
        id = long("id"), name = getString("name"), createdAtEpochMillis = long("createdAtEpochMillis")
    )

    private fun JSONObject.toProduct() = ProductEntity(
        id = long("id"), name = getString("name"), sku = optionalString("sku"),
        priceRupiah = long("priceRupiah"), stockQuantity = long("stockQuantity"),
        categoryId = optionalLong("categoryId"), updatedAtEpochMillis = long("updatedAtEpochMillis")
    )

    private fun JSONObject.toTransaction() = SalesTransactionEntity(
        id = long("id"), transactionNumber = getString("transactionNumber"),
        createdAtEpochMillis = long("createdAtEpochMillis"),
        subtotalRupiah = long("subtotalRupiah"), discountRupiah = long("discountRupiah"),
        taxRupiah = long("taxRupiah"), totalRupiah = long("totalRupiah"),
        paidRupiah = long("paidRupiah"), changeRupiah = long("changeRupiah")
    )

    private fun JSONObject.toTransactionItem() = TransactionItemEntity(
        id = long("id"), transactionId = long("transactionId"),
        productId = optionalLong("productId"), productNameSnapshot = getString("productNameSnapshot"),
        skuSnapshot = optionalString("skuSnapshot"), unitPriceRupiah = long("unitPriceRupiah"),
        quantity = long("quantity"), lineTotalRupiah = long("lineTotalRupiah")
    )

    private fun JSONObject.long(key: String): Long {
        val value = get(key)
        require(value is Number) { "Kolom $key harus berupa angka." }
        return value.toString().toLongOrNull()
            ?: throw IllegalArgumentException("Kolom $key harus berupa bilangan bulat.")
    }

    private fun JSONObject.optionalLong(key: String): Long? {
        val value = opt(key)
        if (value == null || value === JSONObject.NULL) return null
        require(value is Number) { "Kolom $key harus berupa angka atau null." }
        return value.toString().toLongOrNull()
            ?: throw IllegalArgumentException("Kolom $key harus berupa bilangan bulat.")
    }

    private fun JSONObject.optionalString(key: String): String? {
        val value = opt(key)
        if (value == null || value === JSONObject.NULL) return null
        return value as? String
            ?: throw IllegalArgumentException("Kolom $key harus berupa teks atau null.")
    }

    data class RestoreSummary(
        val categoryCount: Int,
        val productCount: Int,
        val transactionCount: Int,
        val transactionItemCount: Int
    )

    companion object {
        private const val FORMAT_NAME = "BERKAH_POS_BACKUP"
        private const val SCHEMA_VERSION = 1
        private const val MAX_BACKUP_BYTES = 25 * 1024 * 1024
        private const val MAX_CATEGORIES = 5_000
        private const val MAX_PRODUCTS = 50_000
        private const val MAX_TRANSACTIONS = 100_000
        private const val MAX_ITEMS = 250_000
    }
}
