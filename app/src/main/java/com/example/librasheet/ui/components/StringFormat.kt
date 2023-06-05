package com.example.librasheet.ui.components

import android.icu.text.NumberFormat
import android.text.format.DateFormat
import androidx.compose.runtime.Stable
import com.example.librasheet.data.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Stable
fun formatDollar(value: Double): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    return format.format(value)
}
@Stable
fun formatDollar(value: Long): String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    return format.format(value.toDoubleDollar())
}

@Stable
fun format2Decimals(value: Double, length: Int = 0): String {
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
fun formatPercent(value: Double): String {
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

private val c = GregorianCalendar()

@Stable
fun formatDateInt(date: Int, format: String): String {
    c.setIntDate(date)
    return DateFormat.format(format, c).toString()
}

@Stable
fun SimpleDateFormat.parseOrNull(x: String) = try {
    parse(x)
} catch (e: ParseException) {
    null
}



@Stable
fun formatOrder(x: Double, order: Int): String {
    val y = x.roundToInt()
    return when (order) {
        1_000_000 -> (y / 1_000_000).toString() + "m"
        1_000 -> (y / 1_000).toString() + "k"
        else -> y.toString()
    }
}