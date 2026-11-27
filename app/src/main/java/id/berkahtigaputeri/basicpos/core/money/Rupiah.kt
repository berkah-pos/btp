package id.berkahtigaputeri.basicpos.core.money

/**
 * Integer-only Rupiah amount. Arithmetic is overflow-checked and never uses floating point.
 */
@JvmInline
value class Rupiah private constructor(val amount: Long) {
    operator fun plus(other: Rupiah): Rupiah =
        from(Math.addExact(amount, other.amount))

    operator fun minus(other: Rupiah): Rupiah {
        require(amount >= other.amount) { "Hasil pengurangan Rupiah tidak boleh negatif." }
        return from(amount - other.amount)
    }

    fun multipliedBy(quantity: Long): Rupiah {
        require(quantity >= 0L) { "Kuantitas tidak boleh negatif." }
        return from(Math.multiplyExact(amount, quantity))
    }

    companion object {
        fun from(amount: Long): Rupiah {
            require(amount >= 0L) { "Nilai Rupiah tidak boleh negatif." }
            return Rupiah(amount)
        }
    }
}
