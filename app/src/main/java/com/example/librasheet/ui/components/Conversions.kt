package com.example.librasheet.ui.components

import kotlin.math.roundToLong

fun Float.toLongDollar() : Long {
    return (this * 10000f).roundToLong()
}

fun Long.toFloatDollar() : Float {
    return (this / 10000f)
}