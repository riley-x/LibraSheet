package com.example.librasheet.data

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.entity.AccountHistory
import com.example.librasheet.ui.graphing.StackedLineGraphValue


interface HistoryEntry {
    val seriesKey: Long
    val date: Int
    val value: Long
}

@Immutable
data class HistoryEntryBase(
    override val seriesKey: Long,
    override val value: Long,
    override val date: Int,
): HistoryEntry

interface Series {
    val key: Long
    val color: Color
}


/** This takes a list of account history, assumed in increasing date order, and folds it into a
 * map accountKey -> balances. The balances of each account will be zero padded so they all have the
 * same length. **/
fun List<HistoryEntry>.alignDates(
    useLastIfAbsent: Boolean = true
): Pair<MutableList<Int>, MutableMap<Long, MutableList<Long>>> {
    /** Outputs **/
    val dates = mutableListOf<Int>()
    val balances = mutableMapOf<Long, MutableList<Long>>()
    if (isEmpty()) return Pair(dates, balances)

    /** Aggregators. We assume the list is in date order, so same dates should be next to each other.
     * Collect values until we find a new date, at which point we update the former date. **/
    var currentDate = this[0].date
    val currentValues = mutableMapOf<Long, Long>()
    fun update(newDate: Int) {
        balances.forEach { (account, list) ->
            list.add(currentValues.getOrElse(account) { if (useLastIfAbsent) list.lastOrNull() ?: 0 else 0 })
        }
        currentValues.clear()
        dates.add(currentDate) // this should happen at end of block not beginning, so that newly added accounts are correctly in-sync.
        currentDate = newDate
    }

    /** Main loop **/
    forEach {
        if (it.date != currentDate) update(it.date)
        if (it.seriesKey !in balances) {
            balances[it.seriesKey] = List(dates.size) { 0L }.toMutableList()
        }
        currentValues[it.seriesKey] = it.value
    }
    update(0)

    return Pair(dates, balances)
}

/**
 * Gets a list of values that can be passed to the stacked line graph.
 * @param series should be in order of bottom of the stack to the top. Values should not be pre-added.
 */
fun Map<Long, List<Long>>.stackedLineGraphValues(
    series: List<Series>,
    multiplier: Float = 1f,
): Triple<List<StackedLineGraphValue>, Float, Float> {
    var minY = 0f
    var maxY = 0f
    val allValues: MutableList<Pair<Color, List<Float>>> = mutableListOf()

    var lastValues: List<Float>? = null
    for (line in series) { // 0th account is top in stack == sum of all other values
        Log.d("Libra/HistoryUtils", "line $line")
        val balances = this[line.key] ?: continue

        val values = mutableListOf<Float>()
        balances.forEachIndexed { index, balance ->
            val value = multiplier * balance.toFloatDollar() + (lastValues?.getOrNull(index) ?: 0f)
            values.add(value)
            if (value < minY) minY = value
            if (value > maxY) maxY = value
        }
        lastValues = values

        allValues.add(0, Pair(line.color, values)) // StackedLineGraph wants top stack at start of list
        Log.d("Libra/HistoryUtils", "values $values")
    }

    return Triple(allValues, minY, maxY)
}
