package id.berkahtigaputeri.basicpos.core.money

import org.junit.Assert.assertEquals
import org.junit.Test

class RupiahFormatterTest {
    @Test
    fun formatsWholeRupiahWithoutFractionDigits() {
        assertEquals("Rp 1.234.567", Rupiah.from(1_234_567L).formatIdr())
    }

    @Test
    fun formatsZeroRupiah() {
        assertEquals("Rp 0", Rupiah.from(0L).formatIdr())
    }
}
