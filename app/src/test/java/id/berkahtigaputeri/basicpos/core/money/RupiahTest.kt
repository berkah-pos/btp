package id.berkahtigaputeri.basicpos.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RupiahTest {
    @Test
    fun additionUsesIntegerArithmetic() {
        assertEquals(15_000L, (Rupiah.from(10_000) + Rupiah.from(5_000)).amount)
    }

    @Test
    fun multiplicationUsesIntegerArithmetic() {
        assertEquals(36_000L, Rupiah.from(12_000).multipliedBy(3).amount)
    }

    @Test
    fun negativeAmountIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Rupiah.from(-1)
        }
    }

    @Test
    fun subtractionCannotProduceNegativeAmount() {
        assertThrows(IllegalArgumentException::class.java) {
            Rupiah.from(1_000) - Rupiah.from(1_500)
        }
    }

    @Test
    fun multiplicationRejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException::class.java) {
            Rupiah.from(1_000).multipliedBy(-1)
        }
    }

    @Test
    fun multiplicationOverflowIsRejected() {
        assertThrows(ArithmeticException::class.java) {
            Rupiah.from(Long.MAX_VALUE).multipliedBy(2)
        }
    }

    @Test
    fun formatsWholeRupiahWithIndonesianGrouping() {
        assertTrue(Rupiah.from(1_234_000).formatIdr().contains("1.234.000"))
    }

    @Test
    fun overflowIsRejected() {
        assertThrows(ArithmeticException::class.java) {
            Rupiah.from(Long.MAX_VALUE) + Rupiah.from(1)
        }
    }
}
