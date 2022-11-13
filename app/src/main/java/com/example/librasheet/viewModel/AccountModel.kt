package com.example.librasheet.viewModel

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.rangeBetween
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.graphing.AxesState
import com.example.librasheet.ui.graphing.DiscreteGraphState
import com.example.librasheet.ui.graphing.autoXTicksDiscrete
import com.example.librasheet.ui.graphing.autoYTicks
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.dataClasses.NamedValue
import com.example.librasheet.viewModel.preview.previewAccount
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewStackedLineGraph
import com.example.librasheet.viewModel.preview.testAccountHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


const val graphYPad = 0.1f
const val graphTicksX = 5
const val graphTicksY = 6

class AccountModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.accountDao()

    val all = mutableStateListOf<Account>()
    var history: MutableList<BalanceHistory> = mutableListOf()
    val incomeGraph = DiscreteGraphState()

    suspend fun load() {
        viewModel.viewModelScope.launch {
            all.addAll(withContext(Dispatchers.IO) {
//                dao.getAccounts()
                previewAccounts
            })
            all.forEach {
                Log.d("Libra/AccountModel/load", "$it")
            }
        }
        viewModel.viewModelScope.launch {
            history = withContext(Dispatchers.IO) {
                dao.getHistory().foldAccounts()
            }
            viewModel.viewModelScope.launch { loadIncomeGraph() }
        }
    }

    @MainThread
    private suspend fun loadIncomeGraph() {
        if (history.size < 2) return

        withContext(Dispatchers.Default) {
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

            val pad = (if (maxY == minY) maxY else (maxY - minY)) * graphYPad
            maxY += pad
            minY -= pad

            val ticksX = autoXTicksDiscrete(values.size, graphTicksX).map {
                NamedValue(
                    value = values[it],
                    name = formatDateInt(history[it + 1].date, "MMM 'yy"), // add one since income values are one less
                )
            }
            val ticksY = autoYTicks(
                minY,
                maxY,
                graphTicksY,
            )
            val axes = AxesState(
                ticksY = ticksY,
                ticksX = ticksX,

            )
        }

        incomeGraph.values.clear()

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
    }
}