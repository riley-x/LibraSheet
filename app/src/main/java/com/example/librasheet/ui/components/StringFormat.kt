package com.example.librasheet.ui.components

import android.icu.text.NumberFormat
import androidx.compose.runtime.Stable

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
fun formatDollarNoSymbol(value: Float, length: Int = 0): String {
    val format = NumberFormat.getNumberInstance()
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    val out = format.format(value)
    return out.padStart(length)
}