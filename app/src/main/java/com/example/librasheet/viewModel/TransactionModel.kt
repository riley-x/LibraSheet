package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.CategoryRule
import com.example.librasheet.data.entity.TransactionEntity
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
    val filter = mutableStateOf(TransactionFilters())
    private var account: Account? = null
    val detail = mutableStateOf(TransactionEntity())


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
        loadList()
    }


    @Callback
    fun load(newAccount: Account? = null) {
        if (account == newAccount) return
        account = newAccount
        filter.value = defaultFilter.copy(account = account?.key)
        loadList()
    }

    fun reload() = loadList()

    private fun loadList() {
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
    }
}


fun List<TransactionEntity>.matchCategories(parentCategory: Category) {
    val keyMap = parentCategory.getKeyMap()
    keyMap[Category.Ignore.key] = Category.Ignore
    forEach {
        it.category = keyMap.getOrDefault(it.categoryKey, Category.None)
    }
}
