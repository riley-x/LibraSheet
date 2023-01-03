package com.example.librasheet.data

import android.icu.util.Calendar
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
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


/**
 * Creates a list of calendar months between [startDate] and the current date. The months are encoded
 * as int dates with the day set to 0 to indicate the last date of the previous month. For example,
 * 2022-12-00 corresponds to Nov 2022.
 *
 * @param [startDate] should be a month end in the format above.
 */
fun createMonthList(startDate: Int): MutableList<Int> {
    val end = thisMonthEnd(Calendar.getInstance().toIntDate())
    val out = mutableListOf<Int>()

    var it = startDate
    while (it <= end) {
        out.add(it)
        it = incrementMonthEnd(it)
    }
    return out
}


/** This takes a list of account history, assumed in increasing date order, and folds it into a
 * map accountKey -> balances. The balances of each account will be zero padded so they all have the
 * same length.
 **/
fun List<HistoryEntry>.alignDates(
    cumulativeSum: Boolean = true
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
            var value = currentValues.getOrDefault(account, 0L)
            if (cumulativeSum) value += list.lastOrNull() ?: 0
            list.add(value)
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
 * As above but with a custom date set. Will throw an error if a date in [this] is not present in
 * [dates].
 *
 * @param dates should be in increasing order
 */
fun List<HistoryEntry>.alignDates(
    dates: List<Int>,
    cumulativeSum: Boolean = false,
): MutableMap<Long, MutableList<Long>> {
    /** Outputs **/
    val balances = mutableMapOf<Long, MutableList<Long>>()
    if (isEmpty()) return balances

    /** Aggregators. We assume the list is in date order, so same dates should be next to each other.
     * Collect values until we find a new date, at which point we update the former date. **/
    var currentDateIt = 0 // This also is the size of each list in [balances]
    val currentValues = mutableMapOf<Long, Long>()
    fun update() {
        balances.forEach { (account, list) ->
            var value = currentValues.getOrDefault(account, 0L)
            if (cumulativeSum) value += list.lastOrNull() ?: 0
            list.add(value)
        }
        currentValues.clear()
        currentDateIt += 1
    }

    /** Main loop **/
    forEach {
        fun getCurrentDate(): Int {
            val date = dates.getOrNull(currentDateIt) ?: throw RuntimeException("alignDates() out of dates at entry $it")
            if (date > it.date) throw RuntimeException("alignDates() mismatched date $date at entry $it")
            return date
        }

        /** Update intermediate dates until we reach the date of the current entry. **/
        var currentDate = getCurrentDate()
        while (currentDate < it.date) {
            update()
            currentDate = getCurrentDate()
        }

        /** Log balance for this date. Don't push back here, wait for date change. **/
        if (it.seriesKey !in balances) {
            balances[it.seriesKey] = List(currentDateIt) { 0L }.toMutableList()
        }
        currentValues[it.seriesKey] = it.value
    }

    /** Update remaining dates if [this] doesn't contain them **/
    while (currentDateIt < dates.size) {
        update()
    }

    return balances
}

/**
 * Gets a list of values that can be passed to the stacked line graph. [this] should be a map as
 * returned by [alignDates]. This function will skip any series that have negative values (after
 * application of [multiplier]).
 *
 * @param series should be in order of bottom of the stack to the top. Values should not be pre-added.
 * @param multiplier factor to multiply the values of [this] by.
 * @param lastSeriesIsTotal the last entry of [series] has been pre-added with all previous entries.
 */
fun Map<Long, List<Long>>.stackedLineGraphValues(
    series: List<Series>,
    multiplier: Float = 1f,
    lastSeriesIsTotal: Boolean = false,
): Triple<List<StackedLineGraphValue>, Float, Float> {
    var minY = 0f
    var maxY = 0f
    val allValues: MutableList<Pair<Color, List<Float>>> = mutableListOf()

    var lastValues: List<Float>? = null
    series@ for (line in series) {
        val balances = this[line.key] ?: continue
        if (lastSeriesIsTotal && line == series.last()) lastValues = null

        val values = mutableListOf<Float>()
        for ((index, balance) in balances.withIndex()) {
            var value = multiplier * balance.toFloatDollar()
            if (value < 0) continue@series
            value += (lastValues?.getOrNull(index) ?: 0f)
            values.add(value)
            if (value < minY) minY = value
            if (value > maxY) maxY = value
        }
        lastValues = values

        allValues.add(0, Pair(line.color, values)) // StackedLineGraph wants top stack at start of list
    }

    return Triple(allValues, minY, maxY)
}
