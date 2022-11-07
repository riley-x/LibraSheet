package com.example.librasheet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize

@Composable
fun IntSize.toDpSize(): DpSize = with (LocalDensity.current) {
    DpSize(width.toDp(), height.toDp())
}