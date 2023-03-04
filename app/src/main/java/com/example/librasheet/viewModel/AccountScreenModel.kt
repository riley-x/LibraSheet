package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.alignDates
import com.example.librasheet.data.dao.AccountDao
import com.example.librasheet.data.dao.CategoryHistoryDao
import com.example.librasheet.data.dao.TransactionDao
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.graphing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AccountScreenState(
    val account: MutableState<Long> = mutableStateOf(0L), // Remember, no storing pointers to accounts!
    val balance: DiscreteGraphState = DiscreteGraphState(),
    val netIncome: NetIncomeGraphState = NetIncomeGraphState(),
    val transactions: SnapshotStateList<TransactionEntity> = mutableStateListOf(),
    val incomeDates: SnapshotStateList<String> = mutableStateListOf(),
    val historyDates: SnapshotStateList<String> = mutableStateListOf(),
)


class AccountScreenModel(
    private val viewModel: LibraViewModel,
) {
    private val categoryRoot = viewModel.categories.data.all
    private val scope = viewModel.viewModelScope
    private val accountDao = viewModel.application.database.accountDao()
    private val categoryHistoryDao = viewModel.application.database.categoryHistoryDao()
    private val transactionDao = viewModel.application.database.transactionDao()

    private val graphYPad = 0.1f
    private val graphTicksX = 4
    private val graphTicksY = 6
    private var cachedDates = listOf<Int>()

    val state = AccountScreenState()

    fun load(months: List<Int>, account: Long = state.account.value) {
        cachedDates = months
        state.account.value = account
        loadIncome(months)
        loadHistory(months)
        loadTransactions()
    }

    private fun loadIncome(dates: List<Int>) = scope.launch {
        val flows = withContext(Dispatchers.IO) {
            categoryHistoryDao.getIncomeAndExpense(state.account.value).alignDates(dates = dates, cumulativeSum = false)
        }
        val income = flows[0] ?: List(dates.size) { 0L }
        val expense = flows[1] ?: List(dates.size) { 0L }
        Log.d("Libra/AccountScreenState/loadIncome", "dates=${dates.takeLast(10)}")
        Log.d("Libra/AccountScreenState/loadIncome", "income=${income.takeLast(10)}")
        Log.d("Libra/AccountScreenState/loadIncome", "expense=${expense.takeLast(10)}")

        state.netIncome.values1.clear()
        state.netIncome.values2.clear()
        state.netIncome.valuesNet.clear()
        state.incomeDates.clear()

        var minY = 0f
        var maxY = 0f
        for (i in income.indices) {
            val inc = income[i].toFloatDollar()
            val exp = expense[i].toFloatDollar()
            if (exp < minY) minY = exp
            if (inc > maxY) maxY = inc
            state.netIncome.values1.add(inc)
            state.netIncome.values2.add(exp)
            state.netIncome.valuesNet.add(inc + exp)
            state.incomeDates.add(formatDateInt(dates[i], "MMM yyyy"))
        }

        val ticksX = autoMonthTicks(dates.first(), dates.last(), graphTicksX)
        val pad = (if (maxY == minY) maxY else (maxY - minY)) * graphYPad
        state.netIncome.axes.value = AxesState(
            ticksY = autoYTicks(minY, maxY, graphTicksY),
            ticksX = ticksX,
            minY = minY - pad,
            maxY = maxY + pad,
            minX = -0.5f,
            maxX = income.size - 0.5f,
        )
    }


    // TODO replace this with a data class, shared with balance screen
    fun loadHistory(dates: List<Int>) = scope.launch {
        state.historyDates.clear()
        dates.mapTo(state.historyDates) { formatDateInt(it, "MMM yyyy") }
        val history = withContext(Dispatchers.IO) {
            accountDao.getHistory(state.account.value).alignDates(dates = dates, cumulativeSum = true)[state.account.value]
                ?: emptyList()
        }
        state.balance.values.clear()
        if (history.isEmpty()) return@launch

        var minY = history.first().toFloatDollar()
        var maxY = minY
        history.forEach {
            val x = it.toFloatDollar()
            state.balance.values.add(x)
            if (x < minY) minY = x
            if (x > maxY) maxY = x
        }

        val ticksX = autoMonthTicks(dates.first(), dates.last(), graphTicksX)
        val pad = (if (maxY == minY) maxY else (maxY - minY)) * graphYPad
        state.balance.axes.value = AxesState(
            ticksY = autoYTicks(minY, maxY, graphTicksY),
            ticksX = ticksX,
            minY = minY - pad,
            maxY = maxY + pad,
            minX = 0f,
            maxX = history.lastIndex.toFloat(),
        )
    }


    fun loadTransactions() = scope.launch {
        val list = withContext(Dispatchers.IO) {
            val list = transactionDao.get(TransactionFilters(
                limit = 100,
                account = state.account.value
            ))
            list.matchAccounts(viewModel.accounts.all)
            list.matchCategories(categoryRoot)
            return@withContext list
        }
        state.transactions.clear()
        state.transactions.addAll(list)
    }
}