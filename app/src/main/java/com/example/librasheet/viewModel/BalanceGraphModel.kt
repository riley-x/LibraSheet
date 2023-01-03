package com.example.librasheet.viewModel

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.alignDates
import com.example.librasheet.data.TimeSeries
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.stackedLineGraphValues
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.components.formatOrder
import com.example.librasheet.ui.graphing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state for the Balance Screen's graphs
 */
class BalanceGraphModel(
    private val viewModel: LibraViewModel,
) {
    private val graphYPad = 0.1f
    private val graphTicksX = 4
    private val graphTicksY = 6

    private val accountDao = viewModel.application.database.accountDao()
    private val categoryHistoryDao = viewModel.application.database.categoryHistoryDao()

    var history: MutableMap<Long, MutableList<Long>> = mutableMapOf()
    var historyDateInts: List<Int> = emptyList()
    var netIncome: MutableList<TimeSeries> = mutableListOf()

    /** State for the net income graph in the balance screen **/
    val incomeGraph = DiscreteGraphState()
    /** State for the history graph in the balance screen **/
    val historyGraph = StackedLineGraphState()
    /** Dates to display when hovering in the balance screen. **/
    val incomeDates = mutableStateListOf<String>()
    val historyDates = mutableStateListOf<String>()

    fun loadIncome(months: List<Int>) = viewModel.viewModelScope.launch {
        netIncome = withContext(Dispatchers.IO) {
            categoryHistoryDao.getNetIncome().alignDates(months)
        }
        Log.d("Libra/BalanceGraphModel/loadIncome", "${netIncome.takeLast(10)}")
        incomeDates.clear()
        netIncome.mapTo(incomeDates) { formatDateInt(it.date, "MMM yyyy") }
        calculateIncomeGraph()
    }


    fun loadHistory(accounts: List<Account>, months: List<Int>) = viewModel.viewModelScope.launch {
        historyDateInts = months
        history = withContext(Dispatchers.IO) {
            accountDao.getHistory().alignDates(dates = months, cumulativeSum = true)
        }
        Log.d("Libra/BalanceGraphModel/loadHistory", "${historyDateInts.takeLast(10)}")
        Log.d("Libra/BalanceGraphModel/loadHistory", "${history}")

        historyDates.clear()
        historyDateInts.mapTo(historyDates) { formatDateInt(it, "MMM yyyy") }
        calculateHistoryGraph(accounts)
    }


    @MainThread
    private suspend fun calculateIncomeGraph() {
        if (netIncome.isEmpty()) return

        val (values, axes) = withContext(Dispatchers.Default) {
            val values = mutableListOf<Float>()
            var minY = 0f
            var maxY = 0f

            netIncome.forEach {
                val value = it.value.toFloatDollar()
                values.add(value)
                if (value < minY) minY = value
                if (value > maxY) maxY = value
            }

            val ticksX = autoXTicksDiscrete(values.size, graphTicksX) {
                formatDateInt(netIncome[it].date, "MMM ''yy") // single quote escapes the date formatters, so need '' to place a literal quote
            }
            val pad = (if (maxY == minY) maxY else (maxY - minY)) * graphYPad
            val axes = AxesState(
                ticksY = autoYTicks(minY, maxY, graphTicksY),
                ticksX = ticksX,
                minY = minY - pad,
                maxY = maxY + pad,
                minX = -0.5f,
                maxX = values.size - 0.5f,
            )

            Pair(values, axes)
        }

        incomeGraph.values.clear()
        incomeGraph.values.addAll(values)
        incomeGraph.axes.value = axes
    }


    @MainThread
    internal suspend fun calculateHistoryGraph(accounts: List<Account>) {
        if (accounts.isEmpty() || history.isEmpty()) return
        if (historyDateInts.size < 2) return

        val (values, axes, order) = withContext(Dispatchers.Default) {
            /** Get values. We use minY = 0 always **/
            val (values, _, maxY) = history.stackedLineGraphValues(accounts)

            /** Create axes **/
            val ticksX = autoXTicksDiscrete(historyDateInts.size, graphTicksX) {
                formatDateInt(historyDateInts[it], "MMM ''yy") // single quote escapes the date formatters, so need '' to place a literal quote
            }
            val (ticksY, order) = autoYTicksWithOrder(0f, maxY, graphTicksY)
            val axes = AxesState(
                ticksY = ticksY,
                ticksX = ticksX,
                minY = 0f,
                maxY = maxY + maxY * graphYPad,
                minX = 0f,
                maxX = historyDateInts.lastIndex.toFloat(),
            )

            Triple(values, axes, order)
        }
        Log.d("Libra/BalanceGraphModel/loadHistoryGraph", "order=$order maxY=${axes.maxY}")

        historyGraph.values.clear()
        historyGraph.values.addAll(values)
        historyGraph.axes.value = axes
        historyGraph.toString.value = { formatOrder(it, order) }
    }


}