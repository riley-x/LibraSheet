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
    return thisMonthEnd(year, month)
}

fun thisMonthEnd(year: Int, month: Int): Int {
    return if (month == 12) getIntDate(year + 1, 1, 0)
    else getIntDate(year, month + 1, 0)
}

fun getMonthFromMonthEnd(date: Int): Int {
    if (date == 0) return 0
    val month = getMonth(date)
    return if (month == 1) 12 else month - 1
}
fun getYearFromMonthEnd(date: Int): Int {
    if (date == 0) return 0
    val month = getMonth(date)
    val year = getYear(date)
    return if (month == 1) year - 1 else year
}
fun getYearAndMonthFromMonthEnd(date: Int): Pair<Int, Int> {
    if (date == 0) return Pair(0, 0)
    val month = getMonth(date)
    val year = getYear(date)
    return if (month == 1) Pair(year - 1, 12)
    else Pair(year, month - 1)
}

/**
 * Assuming [monthYear] is a date in the format YYYYMM00, returns the next [n]th month in the same format.
 * The months are encoded as int dates with the day set to 0 to indicate the last date of the
 * previous month. For example, 20221200 corresponds to Nov 2022.
 */
fun incrementMonthEnd(monthYear: Int, n: Int = 1): Int {
    val year = getYear(monthYear) + n / 12
    val month = getMonth(monthYear) + n % 12
    return if (month > 12) getIntDate(year + 1, month - 12, 0)
    else getIntDate(year, month, 0)
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