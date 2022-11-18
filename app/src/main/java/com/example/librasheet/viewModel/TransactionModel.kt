package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.setDay
import com.example.librasheet.data.toIntDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*



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
    val detailReimbursements = mutableStateListOf<ReimbursementWithValue>()
    val detailAllocations = mutableStateListOf<Allocation>()

    @Callback
    fun save(new: TransactionEntity, old: TransactionEntity) {
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.IO){
                if (old.key > 0) dao.update(new, old)
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
    fun init() {
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
        detailReimbursements.clear() // should clear before the launch so previous detail's allocations don't show
        detailAllocations.clear()
        viewModel.viewModelScope.launch {
            val (reimbs, allocs) = withContext(Dispatchers.IO) {
                dao.getDetails(t)
            }
            detailReimbursements.addAll(reimbs)
            detailAllocations.addAll(allocs)
        }
    }
}


fun List<TransactionEntity>.matchCategories(parentCategory: Category) {
    val keyMap = parentCategory.getKeyMap()
    keyMap[Category.Ignore.key] = Category.Ignore
    forEach {
        it.category = keyMap.getOrDefault(it.categoryKey, Category.None)
    }
}
