package com.example.librasheet.ui.graphing

import com.example.librasheet.viewModel.dataClasses.NamedValue
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * The number of ticks returned is at most maxDivisions. The center nCenter values are chosen.
 */
fun autoYTicks(minY: Float, maxY: Float, maxDivisions: Int, nCenter: Int = maxDivisions): List<NamedValue> {
    /** Start with assuming perfectly even spacing with [min, max, diff / maxDivision]. Increase the
     * step size to the nearest human readable value. Here, stepDesired = step * stepDesiredFactor.
     * Move human readable factors (2, 5, 10) from stepDesiredFactor to step until stepDesiredFactor
     * is just below 1.
     */
    var step = 1
    var stepDesiredFactor = (maxY - minY) / maxDivisions

    while (stepDesiredFactor > .5f) { // TODO doesn't handle cases lower than 0.5
        if (stepDesiredFactor < 1f) { // steps between [0, 1] are rounded up to 1
            break
        } else if (stepDesiredFactor < 2f) { // steps between [1, 2] are rounded up to 2
            step *= 2
            break
        } else if (stepDesiredFactor < 5f) { // steps between [2, 5] are rounded up to 5
            step *= 5
            break
        } else { // multiply by 10 and repeat
            step *= 10
            stepDesiredFactor /= 10f
        }
    }

    /** Create the tick list by starting from the nearest integer step **/
    val start = ceil(minY / step).roundToInt()
    fun label(i: Int): String {
        return if (step % 1_000_000 == 0) ((start + i) * step / 1_000_000).toString() + "m"
        else if (step % 1_000 == 0) ((start + i) * step / 1_000).toString() + "k"
        else ((start + i) * step).toString()
    }
    val output = mutableListOf<NamedValue>()
    for (i in 0 until maxDivisions) {
        val tick = ((start + i) * step).toFloat()
        if (tick < maxY) output.add(NamedValue(tick, label(i)))
    }

    /** Pick the center most values **/
    return if (output.size <= nCenter) {
        output
    } else if ((output.size - nCenter) % 2 == 0) {
        val drop = (output.size - nCenter) / 2
        output.drop(drop).dropLast(drop)
    } else {
        val lowerIsCloser = (output.first().value - minY) < (maxY - output.last().value)
        val drop = (output.size - nCenter) / 2
        if (lowerIsCloser) {
            output.drop(drop + 1).dropLast(drop)
        } else {
            output.drop(drop).dropLast(drop + 1)
        }
    }
}