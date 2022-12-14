package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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


class AccountScreenState(
    private val categoryRoot: Category,
    private val scope: CoroutineScope,
    private val accountDao: AccountDao,
    private val categoryHistoryDao: CategoryHistoryDao,
    private val transactionDao: TransactionDao,
) {
    private val graphYPad = 0.1f
    private val graphTicksX = 4
    private val graphTicksY = 6
    private var cachedDates = listOf<Int>()

    val account = mutableStateOf(0L) // Remember, no storing pointers to accounts!
    val balance = DiscreteGraphState()
    val netIncome = NetIncomeGraphState()
    val transactions = mutableStateListOf<TransactionEntity>()
    val incomeDates = mutableStateListOf<String>()
    val historyDates = mutableStateListOf<String>()

    fun load(months: List<Int>, account: Long = this.account.value) {
        cachedDates = months
        this.account.value = account
        loadIncome(months)
        loadHistory(months)
        loadTransactions()
    }

    fun loadIncome(dates: List<Int>) = scope.launch {
        val flows = withContext(Dispatchers.IO) {
            categoryHistoryDao.getIncomeAndExpense(account.value).alignDates(dates = dates, cumulativeSum = false)
        }
        val income = flows[0] ?: return@launch
        val expense = flows[1] ?: return@launch
        Log.d("Libra/AccountScreenState/loadIncome", "dates=${dates.takeLast(10)}")
        Log.d("Libra/AccountScreenState/loadIncome", "income=${income.takeLast(10)}")
        Log.d("Libra/AccountScreenState/loadIncome", "expense=${expense.takeLast(10)}")

        netIncome.values1.clear()
        netIncome.values2.clear()
        netIncome.valuesNet.clear()
        incomeDates.clear()

        var minY = 0f
        var maxY = 0f
        for (i in income.indices) {
            val inc = income[i].toFloatDollar()
            val exp = expense[i].toFloatDollar()
            if (exp < minY) minY = exp
            if (inc > maxY) maxY = inc
            netIncome.values1.add(inc)
            netIncome.values2.add(exp)
            netIncome.valuesNet.add(inc + exp)
            incomeDates.add(formatDateInt(dates[i], "MMM yyyy"))
        }

        val ticksX = autoXTicksDiscrete(income.size, graphTicksX) {
            formatDateInt(dates[it], "MMM ''yy") // single quote escapes the date formatters, so need '' to place a literal quote
        }
        val pad = (if (maxY == minY) maxY else (maxY - minY)) * graphYPad
        netIncome.axes.value = AxesState(
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
        historyDates.clear()
        dates.mapTo(historyDates) { formatDateInt(it, "MMM yyyy") }
        val history = withContext(Dispatchers.IO) {
            accountDao.getHistory(account.value).alignDates(dates = dates, cumulativeSum = true)[account.value]
                ?: emptyList()
        }
        balance.values.clear()
        if (history.isEmpty()) return@launch

        var minY = history.first().toFloatDollar()
        var maxY = minY
        history.forEach {
            val x = it.toFloatDollar()
            balance.values.add(x)
            if (x < minY) minY = x
            if (x > maxY) maxY = x
        }

        val ticksX = autoXTicksDiscrete(dates.size, graphTicksX) {
            formatDateInt(dates[it], "MMM ''yy") // single quote escapes the date formatters, so need '' to place a literal quote
        }
        val pad = (if (maxY == minY) maxY else (maxY - minY)) * graphYPad
        balance.axes.value = AxesState(
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
                account = account.value
            ))
            list.matchCategories(categoryRoot)
            return@withContext list
        }
        transactions.clear()
        transactions.addAll(list)
    }
}