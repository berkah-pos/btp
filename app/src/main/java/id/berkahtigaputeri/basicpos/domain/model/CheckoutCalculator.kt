package id.berkahtigaputeri.basicpos.domain.model

data class CheckoutLineAmount(
    val unitPriceRupiah: Long,
    val quantity: Long
)

data class CheckoutTotals(
    val lineTotalsRupiah: List<Long>,
    val subtotalRupiah: Long,
    val discountRupiah: Long,
    val taxRupiah: Long,
    val totalRupiah: Long,
    val paidRupiah: Long,
    val changeRupiah: Long
)

/**
 * Pure checkout arithmetic. Every amount is whole Rupiah stored as Long.
 * Math.*Exact fails closed rather than silently wrapping on overflow.
 */
object CheckoutCalculator {
    fun calculate(
        lines: List<CheckoutLineAmount>,
        discountRupiah: Long,
        taxRupiah: Long,
        paidRupiah: Long
    ): CheckoutTotals {
        require(lines.isNotEmpty()) { "Keranjang masih kosong." }
        require(discountRupiah >= 0) { "Diskon tidak boleh negatif." }
        require(taxRupiah >= 0) { "Pajak tidak boleh negatif." }
        require(paidRupiah >= 0) { "Pembayaran tidak boleh negatif." }

        val lineTotals = lines.map { line ->
            require(line.unitPriceRupiah >= 0) { "Harga produk tidak boleh negatif." }
            require(line.quantity > 0) { "Jumlah item harus lebih dari nol." }
            Math.multiplyExact(line.unitPriceRupiah, line.quantity)
        }
        val subtotal = lineTotals.fold(0L) { total, lineTotal -> Math.addExact(total, lineTotal) }
        require(discountRupiah <= subtotal) {
            "Diskon tidak boleh melebihi subtotal."
        }
        val total = Math.addExact(
            Math.subtractExact(subtotal, discountRupiah),
            taxRupiah
        )
        require(paidRupiah >= total) {
            "Uang pembayaran masih kurang."
        }

        return CheckoutTotals(
            lineTotalsRupiah = lineTotals,
            subtotalRupiah = subtotal,
            discountRupiah = discountRupiah,
            taxRupiah = taxRupiah,
            totalRupiah = total,
            paidRupiah = paidRupiah,
            changeRupiah = Math.subtractExact(paidRupiah, total)
        )
    }
}
