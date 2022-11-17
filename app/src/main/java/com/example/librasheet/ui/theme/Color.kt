package com.example.librasheet.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color

val Green500 = Color(0xFF1EB969)
val DarkBlue900 = Color(0xFF26282F)

val CandleGreen = Color(0xFF2DBF78)
val CandleRed = Color(0xFFEF4747)
val Black90 = Color(0xFF1A1A1A)

val DarkColorPalette = darkColors(
    primary = Green500,
    secondary = Green500,
    surface = Black90,
    onSurface = Color.White,
    background = Black90,
    onBackground = Color.White,
    error = Color(0xFFEB5858),
)

fun randomColor() = Color((Math.random() * 16777215).toInt() or (0xFF shl 24))