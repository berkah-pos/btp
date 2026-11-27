package id.berkahtigaputeri.basicpos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CheckoutCalculatorTest {
    @Test
    fun calculatesSubtotalDiscountTaxAndChangeWithIntegerRupiah() {
        val totals = CheckoutCalculator.calculate(
            lines = listOf(
                CheckoutLineAmount(unitPriceRupiah = 12_500, quantity = 2),
                CheckoutLineAmount(unitPriceRupiah = 7_000, quantity = 1)
            ),
            discountRupiah = 2_000,
            taxRupiah = 1_500,
            paidRupiah = 40_000
        )

        assertEquals(listOf(25_000L, 7_000L), totals.lineTotalsRupiah)
        assertEquals(32_000L, totals.subtotalRupiah)
        assertEquals(31_500L, totals.totalRupiah)
        assertEquals(8_500L, totals.changeRupiah)
    }

    @Test
    fun rejectsEmptyCart() {
        assertThrows(IllegalArgumentException::class.java) {
            CheckoutCalculator.calculate(emptyList(), 0, 0, 0)
        }
    }

    @Test
    fun rejectsQuantityZeroAndNegativePrices() {
        assertThrows(IllegalArgumentException::class.java) {
            CheckoutCalculator.calculate(
                listOf(CheckoutLineAmount(unitPriceRupiah = 1_000, quantity = 0)),
                0, 0, 1_000
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            CheckoutCalculator.calculate(
                listOf(CheckoutLineAmount(unitPriceRupiah = -1, quantity = 1)),
                0, 0, 0
            )
        }
    }

    @Test
    fun rejectsDiscountGreaterThanSubtotal() {
        assertThrows(IllegalArgumentException::class.java) {
            CheckoutCalculator.calculate(
                listOf(CheckoutLineAmount(10_000, 1)),
                10_001, 0, 0
            )
        }
    }

    @Test
    fun rejectsInsufficientPayment() {
        assertThrows(IllegalArgumentException::class.java) {
            CheckoutCalculator.calculate(
                listOf(CheckoutLineAmount(10_000, 1)),
                0, 0, 9_999
            )
        }
    }

    @Test
    fun rejectsMultiplicationAndTotalOverflow() {
        assertThrows(ArithmeticException::class.java) {
            CheckoutCalculator.calculate(
                listOf(CheckoutLineAmount(Long.MAX_VALUE, 2)),
                0, 0, Long.MAX_VALUE
            )
        }
        assertThrows(ArithmeticException::class.java) {
            CheckoutCalculator.calculate(
                listOf(CheckoutLineAmount(Long.MAX_VALUE, 1)),
                0, 1, Long.MAX_VALUE
            )
        }
    }
}
