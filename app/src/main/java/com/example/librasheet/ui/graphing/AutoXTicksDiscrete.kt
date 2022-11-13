package com.example.librasheet.ui.graphing

import androidx.core.math.MathUtils
import com.example.librasheet.viewModel.dataClasses.NamedValue
import kotlin.math.roundToInt

fun autoXTicksDiscrete(size: Int, ticks: Int) =
    IntRange(1, ticks).map {
        val stepX = (size.toFloat() / (ticks + 1)).roundToInt()
        MathUtils.clamp(stepX * it, 0, size - 1)
    }