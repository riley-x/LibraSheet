package com.example.librasheet.ui.graphing

import androidx.core.math.MathUtils
import com.example.librasheet.viewModel.dataClasses.NamedValue
import kotlin.math.roundToInt

fun autoXTicksDiscrete(size: Int, ticks: Int): List<Int> {
    val stepX = (size.toFloat() / (ticks + 1)).roundToInt()
    val out = mutableListOf<Int>()
    for (i in 1..ticks) {
        val pos = stepX * i
        if (pos >= 0 && pos < size) out.add(pos)
    }
    return out
}
