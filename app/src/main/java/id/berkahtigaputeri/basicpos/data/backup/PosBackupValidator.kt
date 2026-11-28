package id.berkahtigaputeri.basicpos.data.backup

import id.berkahtigaputeri.basicpos.data.local.entity.CategoryEntity
import id.berkahtigaputeri.basicpos.data.local.entity.ProductEntity
import id.berkahtigaputeri.basicpos.data.local.entity.SalesTransactionEntity
import id.berkahtigaputeri.basicpos.data.local.entity.StoreProfileEntity
import id.berkahtigaputeri.basicpos.data.local.entity.TransactionItemEntity

/**
 * Validates a complete backup snapshot before restore mutates either Room or DataStore.
 *
 * Kept free of Android APIs so the integrity rules can be exercised by local JVM tests.
 */
internal object PosBackupValidator {
    fun validate(
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
            categories.map { it.name.trim() }.toSet().size == categories.size
        ) { "Nama kategori kosong atau ganda." }

        val categoryIds = categories.map { it.id }.toSet()
        require(products.all {
            it.name.isNotBlank() && it.priceRupiah >= 0 && it.stockQuantity >= 0 &&
                it.updatedAtEpochMillis >= 0 &&
                (it.categoryId == null || it.categoryId in categoryIds) &&
                (it.sku == null || it.sku.isNotBlank())
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
                sale.changeRupiah == sale.paidRupiah - sale.totalRupiah &&
                (sale.storeNameSnapshot == null || sale.storeNameSnapshot.isNotBlank()) &&
                (sale.storeNameSnapshot != null ||
                    listOf(
                        sale.storeOwnerSnapshot,
                        sale.storeAddressSnapshot,
                        sale.storePhoneSnapshot,
                        sale.receiptFooterSnapshot
                    ).all { it == null })
        }) { "Nominal atau nomor transaksi pada cadangan tidak valid." }
        require(transactions.map { it.transactionNumber }.toSet().size == transactions.size) {
            "Nomor transaksi ganda pada cadangan."
        }

        require(items.all { item ->
            item.transactionId in transactionIds &&
                (item.productId == null || item.productId > 0) &&
                item.productNameSnapshot.isNotBlank() &&
                item.unitPriceRupiah >= 0 &&
                item.quantity > 0 &&
                item.lineTotalRupiah == Math.multiplyExact(item.unitPriceRupiah, item.quantity) &&
                (item.skuSnapshot == null || item.skuSnapshot.isNotBlank())
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
}
