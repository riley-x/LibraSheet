package com.example.librasheet.data

import androidx.compose.runtime.Immutable

@Immutable
data class TimeSeries (
    val date: Int,
    val value: Long,
)

/**
 * Filters [this] for the entries matching [dates], filling missing entries with 0, or the last
 * known entry if [useLast]. [this] should be in increasing order.
 *
 * @param dates should be in increasing order
 */
fun List<TimeSeries>.alignDates(dates: List<Int>, useLast: Boolean = false): MutableList<TimeSeries> {
    val out = mutableListOf<TimeSeries>()
    if (dates.isEmpty()) return out

    var entry = 0
    dates.forEach {
        while (entry < size && this[entry].date < it) {
            entry += 1
        }
        if (entry < size && this[entry].date == it) {
            out.add(this[entry])
            entry += 1
        } else {
            out.add(TimeSeries(date = it, value = if (useLast && out.isNotEmpty()) out.last().value else 0L))
        }
    }
    return out
}