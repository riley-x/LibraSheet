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
import kotlin.math.absoluteValue

/** For the map of transaction details in the view model **/
const val SettingsTransactionKeyBase = "settings"
const val BalanceTransactionKeyBase = "balance"


@Stable
class TransactionModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.transactionDao()
    private val defaultFilter = TransactionFilters(
        limit = 100
    )

    /** Full list **/
    val displayList = mutableStateListOf<TransactionEntity>()
    val filter = mutableStateOf(defaultFilter)

    /** Reimbursement **/
    val reimbFilter = mutableStateOf(defaultFilter)
    val reimbList = mutableStateListOf<TransactionEntity>()

    @Callback
    fun filter(newFilter: TransactionFilters) {
        if (newFilter == filter.value) return
        filter.value = newFilter
        load()
    }

    @Callback
    fun filterReimb(newFilter: TransactionFilters) {
        if (newFilter == reimbFilter.value) return
        reimbFilter.value = newFilter
        loadReimb()
    }

    @Callback
    fun initList() {
        if (displayList.isEmpty()) load()
    }

    @Callback
    fun initReimb() {
        if (reimbList.isEmpty()) loadReimb()
    }

    fun clearReimb() {
        reimbFilter.value = defaultFilter
        reimbList.clear()
    }

    fun load() {
        viewModel.viewModelScope.launch {
            val filter = filter.value
            val list = withContext(Dispatchers.IO) {
                val list = dao.get(filter)
                list.matchAccounts(viewModel.accounts.all)
                list.matchCategories(viewModel.categories.data.all)
                return@withContext list
            }
            displayList.clear()
            displayList.addAll(list)
        }
    }

    fun loadReimb() {
        viewModel.viewModelScope.launch {
            val filter = reimbFilter.value
            val list = withContext(Dispatchers.IO) {
                val list = dao.get(filter)
                list.matchAccounts(viewModel.accounts.all)
                list.matchCategories(viewModel.categories.data.all)
                return@withContext list
            }
            reimbList.clear()
            reimbList.addAll(list)
        }
    }

    fun save(new: TransactionWithDetails, old: TransactionWithDetails): Boolean {
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
    fun loadDetail(t: TransactionEntity, keyBase: String) {
        val model = TransactionDetailModel(onSave = ::save)
        viewModel.transactionDetails[keyBase] = model

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
            model.load(
                account = viewModel.accounts.all.find(t.accountKey),
                t = TransactionWithDetails(
                    transaction = t,
                    reimbursements = reimbs,
                    allocations = allocs,
                )
            )
        }
    }
}

fun List<TransactionEntity>.matchAccounts(accounts: List<Account>) {
    val keyMap = accounts.getKeyMap()
    forEach {
        it.accountName = keyMap[it.accountKey]?.name ?: ""
    }
}

fun List<TransactionEntity>.matchCategories(parentCategory: Category) {
    val keyMap = parentCategory.getKeyMap()
    keyMap[Category.Ignore.key] = Category.Ignore
    forEach {
        it.category = keyMap.getOrDefault(it.categoryKey, Category.None)
    }
}
