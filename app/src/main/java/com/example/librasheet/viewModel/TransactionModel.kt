package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.dao.TransactionWithDetails
import com.example.librasheet.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.abs


class TransactionModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.transactionDao()
    private val defaultFilter = TransactionFilters(
        limit = 100
    )

    val displayList = mutableStateListOf<TransactionEntity>()
    val filter = mutableStateOf(defaultFilter)

    val detail = mutableStateOf(TransactionEntity())
    val reimbursements = mutableStateListOf<ReimbursementWithValue>()
    val allocations = mutableStateListOf<Allocation>()

    private var oldReimbursements = listOf<ReimbursementWithValue>()
    private var oldAllocations = listOf<Allocation>()

    @Callback
    fun save(newTransaction: TransactionEntity) {
        // Copy old values before they get changed by the ui
        val old = TransactionWithDetails(
            transaction = detail.value,
            reimbursements = oldReimbursements,
            allocations = oldAllocations,
        )
        val new = TransactionWithDetails(
            transaction = newTransaction,
            reimbursements = reimbursements.toList(),
            allocations = allocations.toList()
        )
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.IO){
                if (old.transaction.key > 0) dao.update(new, old)
                else dao.add(new)
            }
            viewModel.updateDependencies(Dependency.TRANSACTION)
        }
    }

    @Callback
    fun filter(newFilter: TransactionFilters) {
        if (newFilter == filter.value) return
        filter.value = newFilter
        load()
    }

    @Callback
    fun initList() {
        if (displayList.isEmpty()) load()
    }

    fun load() {
        viewModel.viewModelScope.launch {
            val filter = filter.value
            val list = withContext(Dispatchers.IO) {
                val list = dao.get(filter)
                list.matchCategories(viewModel.categories.data.all)
                return@withContext list
            }
            displayList.clear()
            displayList.addAll(list)
        }
    }

    @Callback
    fun loadDetail(t: TransactionEntity) {
        detail.value = t
        reimbursements.clear() // should clear before the launch so previous detail's allocations don't show
        allocations.clear()
        viewModel.viewModelScope.launch {
            val (reimbs, allocs) = withContext(Dispatchers.IO) {
                val (reimbs, allocs) = dao.getDetails(t)

                val keyMap = viewModel.categories.data.all.getKeyMap()
                keyMap[Category.Ignore.key] = Category.Ignore

                reimbs.forEach {
                    it.transaction.category = keyMap.getOrDefault(it.transaction.categoryKey, Category.None)
                }
                allocs.forEach {
                    it.category = keyMap.getOrDefault(it.categoryKey, Category.None)
                }

                return@withContext Pair(reimbs, allocs)
            }
            oldReimbursements = reimbs
            oldAllocations = allocs
            reimbursements.addAll(reimbs)
            allocations.addAll(allocs)
        }
    }

    /**
     * Note these only apply to the ui state. The actual database action happens on save.
     */
    @Callback
    fun addReimbursement(t: TransactionEntity) {
        reimbursements.add(
            ReimbursementWithValue(
                transaction = t,
                value = abs(t.valueAfterReimbursements)
            )
        )
    }

    @Callback
    fun deleteReimbursement(index: Int) {
        reimbursements.removeAt(index)
    }

    @Callback
    fun changeReimbursementValue(index: Int, value: Long) {
        if (index !in reimbursements.indices) return
        reimbursements[index] = reimbursements[index].copy(
            value = value
        )
    }

    @Callback
    fun addAllocation(name: String, value: Long, category: Category?) {
        allocations.add(
            Allocation(
                key = 0,
                name = name,
                transactionKey = detail.value.key,
                categoryKey = category?.key ?: 0,
                value = value,
                listIndex = allocations.size, // TODO edit listIndexes on save
            ).also { it.category = category ?: Category.None }
        )
    }
    @Callback
    fun editAllocation(i: Int, name: String, value: Long, category: Category?) {
        allocations[i] = Allocation(
            key = 0,
            name = name,
            transactionKey = detail.value.key,
            categoryKey = category?.key ?: 0,
            value = value,
            listIndex = i, // TODO edit listIndexes on save
        ).also { it.category = category ?: Category.None }
    }
    @Callback
    fun reorderAllocation(start: Int, end: Int) {
        if (start !in allocations.indices || end !in allocations.indices) return
        allocations.add(end, allocations.removeAt(start))
        // TODO database
    }
    @Callback
    fun deleteAllocation(index: Int) {
        allocations.removeAt(index)
    }

}


fun List<TransactionEntity>.matchCategories(parentCategory: Category) {
    val keyMap = parentCategory.getKeyMap()
    keyMap[Category.Ignore.key] = Category.Ignore
    forEach {
        it.category = keyMap.getOrDefault(it.categoryKey, Category.None)
    }
}
