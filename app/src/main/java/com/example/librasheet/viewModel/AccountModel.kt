package com.example.librasheet.viewModel

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.rangeBetween
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.components.format1Decimal
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.dataClasses.NamedValue
import com.example.librasheet.viewModel.preview.*
import kotlinx.coroutines.*


const val graphYPad = 0.1f
const val graphTicksX = 4
const val graphTicksY = 6

class AccountModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.accountDao()

    private var history: MutableList<BalanceHistory> = mutableListOf()

    /** A list of all accounts and their current balances. This is used throughout the app **/
    val all = mutableStateListOf<Account>()
    /** State for the net income graph in the balance screen **/
    val incomeGraph = DiscreteGraphState()
    /** State for the history graph in the balance screen **/
    val historyGraph = StackedLineGraphState()
    /** Dates to display when hovering in the balance screen. Note that the income graph will drop
     * the first date since its size is one less than the history graph. **/
    val dates = mutableStateListOf<String>()

    fun loadData(): List<Job> {
        val job1 = viewModel.viewModelScope.launch {
            all.addAll(withContext(Dispatchers.IO) {
//                dao.getAccounts()
                previewAccounts
            })
            all.forEach {
                Log.d("Libra/AccountModel/load", "$it")
            }
        }
        val job2 = viewModel.viewModelScope.launch {
            history = withContext(Dispatchers.IO) {
//                dao.getHistory().foldAccounts()
                testHistory.toMutableList()
            }
            dates.clear()
            history.mapTo(dates) { formatDateInt(it.date, "MMM yyyy") }
        }
        return listOf(job1, job2)
    }

    fun loadUi() {
        viewModel.viewModelScope.launch { loadIncomeGraph() }
        viewModel.viewModelScope.launch { loadHistoryGraph() }
    }

    @MainThread
    private suspend fun loadIncomeGraph() {
        if (history.size < 2) return

        val (values, axes) = withContext(Dispatchers.Default) {
            val values = mutableListOf<Float>()
            var minY = 0f
            var maxY = 0f

            var lastValue = history[0].total
            for (i in 1..history.lastIndex) {
                val value = history[i].total
                val income = (value - lastValue).toFloatDollar()
                lastValue = value

                values.add(income)
                if (income < minY) minY = income
                if (income > maxY) maxY = income
            }


            val ticksX = autoXTicksDiscrete(values.size, graphTicksX).map {
                NamedValue(
                    value = it.toFloat(),
                    name = formatDateInt(history[it + 1].date, "MMM ''yy"), // add one since income values are one less
                )
            }
            val ticksY = autoYTicks(
                minY,
                maxY,
                graphTicksY,
            )

            val pad = (if (maxY == minY) maxY else (maxY - minY)) * graphYPad
            val axes = AxesState(
                ticksY = ticksY,
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
    private suspend fun loadHistoryGraph() {
        if (history.size < 2) return
        if (all.isEmpty()) return

        val (values, axes, order) = withContext(Dispatchers.Default) {
            val values = all.reversed().associateBy(
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
            val ticksX = autoXTicksDiscrete(history.size, graphTicksX).map {
                NamedValue(
                    value = it.toFloat(),
                    name = formatDateInt(history[it].date, "MMM ''yy"),
                )
            }
            val (ticksY, order) = autoYTicksWithOrder(
                0f,
                maxY,
                graphTicksY,
            )
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


    @Callback
    fun rename(index: Int, name: String) {
        if (name == all[index].name) return
        all[index] = all[index].copy(name = name)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.update(all[index])
        }
    }

    @Callback
    fun add(name: String) {
        val accountWithoutKey = Account(
            name = name,
            color = randomColor(),
            listIndex = all.size,
            // TODO institute
        )
        viewModel.viewModelScope.launch {
            // TODO loading indicator
            all.add(accountWithoutKey.copy(
                key = withContext(Dispatchers.IO) { dao.add(accountWithoutKey) }
            ))
        }
    }

    @Callback
    fun reorder(startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return
        all.add(endIndex, all.removeAt(startIndex))

        val staleEntities = mutableListOf<Account>()
        for (i in rangeBetween(startIndex, endIndex)) {
            all[i].listIndex = i
            staleEntities.add(all[i])
        }

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.update(all.slice(startIndex..endIndex))
        }
        viewModel.viewModelScope.launch { loadHistoryGraph() }
    }
}