package com.example.librasheet.ui.components

import android.icu.text.NumberFormat
import android.text.format.DateFormat
import androidx.compose.runtime.Stable
import com.example.librasheet.data.*

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

@Stable
fun format1Decimal(value: Float, length: Int = 0): String {
    val format = NumberFormat.getNumberInstance()
    format.isGroupingUsed = false
    format.minimumFractionDigits = 1
    format.maximumFractionDigits = 1
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
fun formatDateIntSimple(date: Int, separator: String = "/"): String {
    if (date == 0) return ""
    val day = getDay(date)
    val month = getMonth(date)
    val year = getYearShort(date).toString().padStart(2, '0')
    return "$month$separator$day$separator$year"
}

@Stable
fun formatDateInt(date: Int, format: String): String {
    return DateFormat.format(format, date.toTimestamp()).toString()
}