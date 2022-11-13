package com.example.librasheet.viewModel

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.BalanceHistory
import com.example.librasheet.data.dao.TimeSeries
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.components.format1Decimal
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.viewModel.dataClasses.NamedValue
import com.example.librasheet.viewModel.preview.testHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BalanceGraphModel(
    private val viewModel: LibraViewModel,
) {
    private val accountDao = viewModel.application.database.accountDao()
    private val categoryHistoryDao = viewModel.application.database.categoryHistoryDao()

    private var history: MutableList<BalanceHistory> = mutableListOf()
    private var netIncome: MutableList<TimeSeries> = mutableListOf()

    /** State for the net income graph in the balance screen **/
    val incomeGraph = DiscreteGraphState()
    /** State for the history graph in the balance screen **/
    val historyGraph = StackedLineGraphState()
    /** Dates to display when hovering in the balance screen. Note that the income graph will drop
     * the first date since its size is one less than the history graph. **/
    val dates = mutableStateListOf<String>()

    fun load(accounts: List<Account>): List<Job> {
        val job1 = viewModel.viewModelScope.launch {
            history = withContext(Dispatchers.IO) {
//                accountDao.getHistory().foldAccounts()
                testHistory.toMutableList()
            }
            dates.clear()
            history.mapTo(dates) { formatDateInt(it.date, "MMM yyyy") }
            loadHistoryGraph(accounts)
        }
        val job2 = viewModel.viewModelScope.launch {
            netIncome = withContext(Dispatchers.IO) {
                categoryHistoryDao.getNetIncome()
            }.toMutableList()
            loadIncomeGraph()
        }
        return listOf(job1, job2)
    }


    @MainThread
    private suspend fun loadIncomeGraph() {
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
    private suspend fun loadHistoryGraph(accounts: List<Account>) {
        if (history.size < 2) return
        if (accounts.isEmpty()) return

        val (values, axes, order) = withContext(Dispatchers.Default) {
            val values = accounts.reversed().associateBy(
                keySelector = { it.key },
                valueTransform = { Pair(it.color, mutableListOf<Float>()) }
            )

            /** Get values and maximum y value **/
            var maxY = 0f
            history.forEach { date ->
                var total = 0f
                values.forEach { key, (_, list) ->
                    total += date.balances.getOrDefault(key, 0).toFloatDollar()
                    list.add(total)
                }
                if (total > maxY) maxY = total
            }

            /** Create axes **/
            val ticksX = autoXTicksDiscrete(history.size, graphTicksX) {
                formatDateInt(history[it].date, "MMM ''yy") // single quote escapes the date formatters, so need '' to place a literal quote
            }
            val (ticksY, order) = autoYTicksWithOrder(0f, maxY, graphTicksY)
            val axes = AxesState(
                ticksY = ticksY,
                ticksX = ticksX,
                minY = 0f,
                maxY = maxY + maxY * graphYPad,
                minX = 0f,
                maxX = history.lastIndex.toFloat(),
            )

            Triple(values.values.reversed().toMutableList(), axes, order)
        }
        Log.d("Libra/AccountModel/loadHistoryGraph", "order=$order maxY=${axes.maxY}")

        historyGraph.values.clear()
        historyGraph.values.addAll(values)
        historyGraph.axes.value = axes
        historyGraph.toString.value = { format1Decimal(it / order) }
    }


}