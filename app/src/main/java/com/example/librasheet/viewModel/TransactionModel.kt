package com.example.librasheet.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
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
import kotlin.math.abs


@Immutable
data class TransactionWithDetails(
    val transaction: MutableState<TransactionEntity> = mutableStateOf(TransactionEntity()),
    val reimbursements: SnapshotStateList<ReimbursementWithValue> = mutableStateListOf(),
    val allocations: SnapshotStateList<Allocation> = mutableStateListOf(),
)


class TransactionModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.transactionDao()
    private val defaultFilter = TransactionFilters(
        limit = 100
    )

    val displayList = mutableStateListOf<TransactionEntity>()
    val filter = mutableStateOf(defaultFilter)
    val detail = TransactionWithDetails()

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
        detail.transaction.value = t
        detail.reimbursements.clear() // should clear before the launch so previous detail's allocations don't show
        detail.allocations.clear()
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
            detail.reimbursements.addAll(reimbs)
            detail.allocations.addAll(allocs)
        }
    }

    /**
     * Note this only applies to the ui state. The actual database action happens on save.
     */
    @Callback
    fun addReimbursement(t: TransactionEntity) {
        detail.reimbursements.add(
            ReimbursementWithValue(
                transaction = t,
                value = minOf(abs(detail.transaction.value.value), abs(t.valueAfterReimbursements))
            )
        )
    }
}


fun List<TransactionEntity>.matchCategories(parentCategory: Category) {
    val keyMap = parentCategory.getKeyMap()
    keyMap[Category.Ignore.key] = Category.Ignore
    forEach {
        it.category = keyMap.getOrDefault(it.categoryKey, Category.None)
    }
}
