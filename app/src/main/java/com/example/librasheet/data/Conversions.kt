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


fun nextMonthEnd(date: Int): Int {
    val month = getMonth(date)
    val year = getYear(date)
    return if (month == 12) getIntDate(year + 1, 1, 0)
    else getIntDate(year, month + 1, 0)
}

fun Calendar.toIntDate() : Int {
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

fun Float.toLongDollar() : Long {
    return (this * 10000f).roundToLong()
}

fun Long.toFloatDollar() : Float {
    return (this / 10000f)
}

fun rangeBetween(a: Int, b: Int) = if (a < b) IntRange(a, b) else IntRange(b, a)