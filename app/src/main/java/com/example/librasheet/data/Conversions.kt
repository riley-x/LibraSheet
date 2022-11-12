package com.example.librasheet.data

import kotlin.math.roundToLong

fun getIntDate(year: Int, month: Int, day: Int): Int {
    return year * 10000 + month * 100 + day
}


fun getDay(date: Int) = date % 100
fun getMonth(date: Int) = (date / 100) % 100
fun getYear(date: Int) = (date / 10000)
fun getYearShort(date: Int) = (date / 10000) % 100

fun nextMonthEnd(date: Int): Int {
    val month = getMonth(date)
    val year = getYear(date)
    return if (month == 12) getIntDate(year + 1, 1, 0)
    else getIntDate(year, month + 1, 0)
}

fun Float.toLongDollar() : Long {
    return (this * 10000f).roundToLong()
}

fun Long.toFloatDollar() : Float {
    return (this / 10000f)
}

fun rangeBetween(a: Int, b: Int) = if (a < b) IntRange(a, b) else IntRange(b, a)