package com.example.librasheet.data

import java.util.*
import kotlin.math.roundToLong

fun getIntDate(year: Int, month: Int, day: Int): Int {
    return year * 10000 + month * 100 + day
}


fun getDay(date: Int) = date % 100
fun getMonth(date: Int) = (date / 100) % 100
fun getYear(date: Int) = (date / 10000)
fun getYearShort(date: Int) = (date / 10000) % 100

fun Int.setDay(day: Int): Int {
    return (this - getDay(this)) + day
}
fun Int.addYears(years: Int) = this + years * 1_00_00


fun thisMonthEnd(date: Int): Int {
    val month = getMonth(date)
    val year = getYear(date)
    return if (month == 12) getIntDate(year + 1, 1, 0)
    else getIntDate(year, month + 1, 0)
}
fun monthDiff(end: Int, start: Int): Int {
    val years = getYear(end) - getYear(start)
    val months = getMonth(end) - getMonth(start)
    return years * 12 + months
}

fun Date.toIntDate(): Int {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal.toIntDate()
}

fun java.util.Calendar.toIntDate() : Int {
    val year: Int = get(Calendar.YEAR)
    val month: Int = get(Calendar.MONTH) + 1 // months are 0-indexed
    val day: Int = get(Calendar.DAY_OF_MONTH)
    return getIntDate(year, month, day)
}
fun android.icu.util.Calendar.toIntDate() : Int {
    val year: Int = get(Calendar.YEAR)
    val month: Int = get(Calendar.MONTH) + 1 // months are 0-indexed
    val day: Int = get(Calendar.DAY_OF_MONTH)
    return getIntDate(year, month, day)
}

fun Calendar.setIntDate(date: Int) {
    set(getYear(date), getMonth(date) - 1, getDay(date)) // months are 0-indexed
}

fun Int.toCalendar() =
    GregorianCalendar(getYear(this), getMonth(this) - 1, getDay(this)) // Named arguments are not allowed for non-Kotlin functions
fun Int.toTimestamp() = toCalendar().time

/** Warning float precision is pretty bad! Is double sufficient? **/
fun Double.toLongDollar() : Long {
    val dollars = this.toLong()
    val fraction = ((this - dollars) * 10000).roundToLong()
    return dollars * 10000 + fraction
}

/** TODO Warning float precision is pretty bad! Is double sufficient? **/
fun Long.toFloatDollar() : Float {
    return (this / 10000) + (this % 10000) / 10000f
}

fun rangeBetween(a: Int, b: Int) = if (a < b) IntRange(a, b) else IntRange(b, a)