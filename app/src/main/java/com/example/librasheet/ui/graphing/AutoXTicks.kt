package com.example.librasheet.ui.graphing

import com.example.librasheet.data.*
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.viewModel.dataClasses.NamedValue
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Creates equally-spaced x ticks for a discrete x axis with [size] points.
 *
 * @param ticks is the approximate number of ticks to display. The exact value is not guaranteed.
 * @param labelFormat returns the label for the given index/point
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

/**
 * Returns the nearest divisor/multiple of 12.
 */
fun roundToNearest12(x: Int): Int =
    when {
        x <= 1 -> 1
        x == 2 -> 2
        x == 3 -> 3
        x == 4 -> 4
        x in 5..8 -> 6
        x in 9..18 -> 12
        else -> 12 * ((x + 5) / 12)
    }


/**
 * Creates concise tick marks given a start and end month in the YYYYMM00 format. Prioritizes showing
 * year changes on each January, and formats remaining ticks with a short month name.
 *
 * @param ticks is the approximate number of ticks to display. It is not guaranteed.
 */
fun autoMonthTicks(start: Int, end: Int, ticks: Int): List<NamedValue> {
    val out = mutableListOf<NamedValue>()

    val stepDesired = monthDiff(end, start).toFloat() / ticks
    val stepSize = roundToNearest12(stepDesired.roundToInt())

    /** I.e. if stepSize = 4, we want to start on month indices 0, 4, or 8 (Jan, May, Sep) **/
    val monthIndexBase = min(stepSize, 12)
    var startOffset = (getMonthFromMonthEnd(start) - 1) % monthIndexBase
    if (startOffset > 0) startOffset = monthIndexBase - startOffset

    var currDate = incrementMonthEnd(start, startOffset)
    var currIndex = startOffset
    while (currDate <= end) {
        val (year, month) = getYearAndMonthFromMonthEnd(currDate)
        out.add(NamedValue(
            value = currIndex.toFloat(),
            name = if (month == 1) "$year" else formatDateInt(currDate, "MMM"),
        ))
        currDate = incrementMonthEnd(currDate, stepSize)
        currIndex += stepSize
    }
    return out
}
