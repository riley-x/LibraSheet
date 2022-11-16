package com.example.librasheet.viewModel.preview

import com.example.librasheet.data.HistoryEntryBase
import com.example.librasheet.data.dao.CategoryDao
import com.example.librasheet.data.dao.CategoryHistoryDao
import com.example.librasheet.data.dao.CategoryWithChildren
import com.example.librasheet.data.dao.TimeSeries
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.CategoryHistory
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

internal val emptyScope = CoroutineScope(EmptyCoroutineContext)

internal class FakeCategoryDao: CategoryDao {
    override fun getIncome(): List<CategoryWithChildren> {
        TODO("Not yet implemented")
    }

    override fun getExpense(): List<CategoryWithChildren> {
        TODO("Not yet implemented")
    }

    override fun getMaxKey(): Long {
        TODO("Not yet implemented")
    }

    override fun add(category: Category): Long {
        TODO("Not yet implemented")
    }

    override fun update(category: Category) {
        TODO("Not yet implemented")
    }

    override fun update(categories: List<Category>) {
        TODO("Not yet implemented")
    }

    override fun delete(category: Category) {
        TODO("Not yet implemented")
    }

    override fun delete(categories: List<Category>) {
        TODO("Not yet implemented")
    }

    override fun unmatchRules(categoryKeys: List<Long>) {
        TODO("Not yet implemented")
    }

    override fun unmatchTransactions(categoryKeys: List<Long>) {
        TODO("Not yet implemented")
    }
}


internal class FakeHistoryDao: CategoryHistoryDao {
    override fun add(it: CategoryHistory) {
        TODO("Not yet implemented")
    }

    override fun getIncome(account: Long): List<TimeSeries> {
        TODO("Not yet implemented")
    }

    override fun getExpense(account: Long): List<TimeSeries> {
        TODO("Not yet implemented")
    }

    override fun getNetIncome(account: Long): List<TimeSeries> {
        TODO("Not yet implemented")
    }

    override fun getNetIncome(): List<TimeSeries> {
        TODO("Not yet implemented")
    }

    override fun getIncomeAndExpense(account: Long): List<HistoryEntryBase> {
        TODO("Not yet implemented")
    }

    override fun getAverages(startDate: Int, endDate: Int): Map<Long, Long> {
        TODO("Not yet implemented")
    }

    override fun getAverages(): Map<Long, Long> {
        TODO("Not yet implemented")
    }

    override fun getDate(date: Int): Map<Long, Long> {
        TODO("Not yet implemented")
    }

    override fun getAll(): List<CategoryHistory> {
        TODO("Not yet implemented")
    }

    override fun getTotals(startDate: Int): Map<Long, Long> {
        TODO("Not yet implemented")
    }
}
