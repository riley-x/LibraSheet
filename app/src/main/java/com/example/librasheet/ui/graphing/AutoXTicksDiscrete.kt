package com.example.librasheet.ui.graphing

import com.example.librasheet.viewModel.dataClasses.NamedValue
import kotlin.math.roundToInt

/**
 * Creates equally-spaced x ticks for a discrete x axis with [size] points.
 *
 * @param ticks is the approximate number of ticks to display. The exact value is not guaranteed.
 */
fun autoXTicksDiscrete(size: Int, ticks: Int, labelFormat: (Int) -> String): List<NamedValue> {
    fun toNamedValue(it: Int) = NamedValue(
        value = it.toFloat(),
        name = labelFormat(it)
    )
    val stepX = ((size - 1).toFloat() / ticks).roundToInt()

    /** Edge case when few data points **/
    if (stepX <= 1) return List(size, ::toNamedValue)

    val indices = mutableListOf<Int>()
    var pos = stepX / 2
    while (pos < size) {
        indices.add(pos)
        pos += stepX
    }
    return indices.map(::toNamedValue)
}
