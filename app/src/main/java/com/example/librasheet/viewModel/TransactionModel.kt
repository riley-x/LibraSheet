package com.example.librasheet.viewModel

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.dao.TransactionWithDetails
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.data.toIntDate
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.formatDateIntSimple
import com.example.librasheet.ui.components.parseOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


@Stable
class TransactionModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.transactionDao()
    private val defaultFilter = TransactionFilters(
        limit = 100
    )

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("MM-dd-yy").apply { isLenient = false }

    val displayList = mutableStateListOf<TransactionEntity>()
    val filter = mutableStateOf(defaultFilter)

    /** Edit/details screen **/
    val detailAccount = mutableStateOf<Account?>(null)
    val detailCategory = mutableStateOf<Category?>(null)
    val detailName = mutableStateOf("")
    val detailDate = mutableStateOf("")
    val detailValue = mutableStateOf("")
    val reimbursements = mutableStateListOf<ReimbursementWithValue>()
    val allocations = mutableStateListOf<Allocation>()

    val dateError = mutableStateOf(false)
    val valueError = mutableStateOf(false)

    /** Old edit/details **/
    private var oldDetail = TransactionEntity()
    private var oldReimbursements = listOf<ReimbursementWithValue>()
    private var oldAllocations = listOf<Allocation>()

    fun isIncome() = (detailValue.value.toFloatOrNull() ?: 0f) > 0f

    private fun createTransaction(): TransactionEntity? {
        val dateInt = formatter.parseOrNull(detailDate.value)?.toIntDate()
        val valueLong = detailValue.value.toFloatOrNull()?.toLongDollar()

        dateError.value = dateInt == null
        valueError.value = valueLong == null
        if (dateError.value || valueError.value) return null

        return TransactionEntity(
            key = oldDetail.key,
            name = detailName.value,
            date = dateInt ?: 0,
            value = valueLong ?: 0L,
            category = detailCategory.value ?: Category.None,
            categoryKey = detailCategory.value?.key ?: 0,
            accountKey = detailAccount.value?.key ?: 0,
        )
    }



    @Callback
    fun save(): Boolean {
        val t = createTransaction() ?: return false

        // Copy old values before they get changed by the ui
        val old = TransactionWithDetails(
            transaction = oldDetail,
            reimbursements = oldReimbursements,
            allocations = oldAllocations,
        )
        val new = TransactionWithDetails(
            transaction = t,
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
        return true
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
        oldDetail = t
        dateError.value = false
        valueError.value = false

        detailAccount.value = viewModel.accounts.all.find(t.accountKey)
        detailCategory.value = viewModel.categories.data.all.find(t.categoryKey)
        detailName.value = t.name
        detailDate.value = formatDateIntSimple(t.date, "-")
        detailValue.value = if (t.value == 0L) "" else t.value.toFloatDollar().toString()

        reimbursements.clear() // should clear before the launch so previous detail's allocations don't show
        allocations.clear()
        viewModel.viewModelScope.launch {
            val (reimbs, allocs) = withContext(Dispatchers.IO) {
                val (reimbs, allocs) = dao.getDetails(t)

                val keyMap = viewModel.categories.data.all.getKeyMap()

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
                transactionKey = oldDetail.key,
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
            transactionKey = oldDetail.key,
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
