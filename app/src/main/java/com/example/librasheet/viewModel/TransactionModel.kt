package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateOf
import com.example.librasheet.data.entity.TransactionEntity

class TransactionModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.transactionDao()

    /** Current detailed transaction in the settings tab **/
    val settingsDetail = mutableStateOf(TransactionEntity())
    /** Current detailed transaction in the balance tab **/
    val balanceDetail = mutableStateOf(TransactionEntity())

    @Callback
    fun save(new: TransactionEntity, old: TransactionEntity) {
        if (old.key > 0) dao.update(new, old)
        else dao.add(new)
    }

    fun add(t: TransactionEntity) {

    }
}