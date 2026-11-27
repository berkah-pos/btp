package id.berkahtigaputeri.basicpos.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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
    fun overflowIsRejected() {
        assertThrows(ArithmeticException::class.java) {
            Rupiah.from(Long.MAX_VALUE) + Rupiah.from(1)
        }
    }
}
