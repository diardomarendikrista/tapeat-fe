package com.group2.tapeat.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Helper untuk mengubah angka Double menjadi format Rupiah.
 */
fun formatRupiah(price: Double): String {
    val localeID = Locale.forLanguageTag("id-ID")
    return NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }.format(price)
}
