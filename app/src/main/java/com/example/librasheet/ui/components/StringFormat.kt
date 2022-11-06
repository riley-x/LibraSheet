package com.example.librasheet.ui.components

import android.icu.text.NumberFormat
import androidx.compose.runtime.Stable
import com.example.librasheet.data.getDay
import com.example.librasheet.data.getMonth
import com.example.librasheet.data.getYearShort
import com.example.librasheet.data.toFloatDollar

@Stable
fun formatDollar(value: Float): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    return format.format(value)
}
@Stable
fun formatDollar(value: Long): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    return format.format(value.toFloatDollar())
}

@Stable
fun format2Decimals(value: Float, length: Int = 0): String {
    val format = NumberFormat.getNumberInstance()
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    val out = format.format(value)
    return out.padStart(length)
}

// NOTE: do not pre-multiply by 100!
@Stable
fun formatPercent(value: Float): String {
    val format = NumberFormat.getPercentInstance()
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    return format.format(value)
}


@Stable
fun formatDateInt(date: Int): String {
    if (date == 0) return ""
    val day = getDay(date)
    val month = getMonth(date)
    val year = getYearShort(date).toString().padStart(2, '0')
    return "$month/$day/$year"
}
