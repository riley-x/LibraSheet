package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.entity.Account
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
        startDate = Calendar.getInstance().toIntDate().setDay(0)
    )

    /** Current list to show in settings tab **/
    val settingsList = mutableStateListOf<TransactionEntity>()
    val settingsFilter = mutableStateOf(TransactionFilters())

    /** Current list to show in balance tab **/
    val balanceList = mutableStateListOf<TransactionEntity>()
    val balanceFilter = mutableStateOf(TransactionFilters())

    /** Current detailed transaction in the settings tab **/
    val settingsDetail = mutableStateOf(TransactionEntity())
    /** Current detailed transaction in the balance tab **/
    val balanceDetail = mutableStateOf(TransactionEntity())

    @Callback
    fun save(new: TransactionEntity, old: TransactionEntity) {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            if (old.key > 0) dao.update(new, old)
            else dao.add(new)
        }
        loadFilter(balanceList, balanceFilter.value)
        loadFilter(settingsList, settingsFilter.value)
        viewModel.updateDependencies(Dependency.TRANSACTION)
    }

    @Callback
    fun filterSettings(filter: TransactionFilters) {
        if (filter == settingsFilter.value) return
        settingsFilter.value = filter
        loadFilter(settingsList, filter)
    }

    @Callback
    fun filterBalance(filter: TransactionFilters) {
        if (filter == balanceFilter.value) return
        balanceFilter.value = filter
        loadFilter(balanceList, filter)
    }

    /**
     * We load these lists lazily, when the user navigates to the respective screens. Don't want to
     * reload though if they change tabs/screens.
     */
    @Callback
    fun loadSettings() {
        if (settingsList.isNotEmpty()) return
        filterSettings(defaultFilter)
    }

    /**
     * We load these lists lazily, when the user navigates to the respective screens. Don't want to
     * reload though if they change tabs/screens.
     */
    @Callback
    fun loadBalance(account: Account) {
        if (balanceList.isNotEmpty()) return
        filterBalance(defaultFilter.copy(account = account))
    }

    private fun loadFilter(outList: SnapshotStateList<TransactionEntity>, filter: TransactionFilters) {
        viewModel.viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                dao.get(filter)
                // TODO need to match category objects
            }
            outList.clear()
            outList.addAll(list)
        }
    }
}