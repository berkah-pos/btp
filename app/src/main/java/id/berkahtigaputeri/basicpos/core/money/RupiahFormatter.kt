package id.berkahtigaputeri.basicpos.core.money

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats whole-Rupiah values for Indonesian users without decimal digits.
 * The formatter is created per call because NumberFormat is not thread-safe.
 */
fun Rupiah.formatIdr(): String {
    val formatter = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
        isGroupingUsed = true
    }
    return "Rp ${formatter.format(amount)}"
}
