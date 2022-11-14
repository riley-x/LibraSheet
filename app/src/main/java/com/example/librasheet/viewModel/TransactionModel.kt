package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.transactionDao()

    /** Current list to show in settings tab **/
    val settingsList = mutableStateListOf<TransactionEntity>()
    /** Current list to show in balance tab **/
    val balanceList = mutableStateListOf<TransactionEntity>()

    /** Current detailed transaction in the settings tab **/
    val settingsDetail = mutableStateOf(TransactionEntity())
    /** Current detailed transaction in the balance tab **/
    val balanceDetail = mutableStateOf(TransactionEntity())

    @Callback
    fun save(new: TransactionEntity, old: TransactionEntity) {
        // TODO update state lists
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            if (old.key > 0) dao.update(new, old)
            else dao.add(new)
        }
    }
}