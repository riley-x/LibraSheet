package com.example.librasheet.ui.graphing

import com.example.librasheet.viewModel.dataClasses.NamedValue
import kotlin.math.roundToInt

fun autoXTicksDiscrete(size: Int, ticks: Int, labelFormat: (Int) -> String): List<NamedValue> {
    val stepX = (size.toFloat() / (ticks + 1)).roundToInt()
    val indices = mutableListOf<Int>()
    for (i in 1..ticks) { // pick the center values (dropping 0 and ticks + 1)
        val pos = stepX * i
        if (pos >= 0 && pos < size) indices.add(pos)
    }
    return indices.map {
        NamedValue(
            value = it.toFloat(),
            name = labelFormat(it),
        )
    }
}
