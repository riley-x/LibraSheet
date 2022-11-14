package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateOf
import com.example.librasheet.data.entity.TransactionEntity

class TransactionModel(
    private val viewModel: LibraViewModel,
) {
    /** Current detailed transaction in the settings tab **/
    val settingsDetail = mutableStateOf(TransactionEntity())
    /** Current detailed transaction in the balance tab **/
    val balanceDetail = mutableStateOf(TransactionEntity())

    @Callback
    fun save(t: TransactionEntity) {
        if (t.key == 0L) {

        } else {

        }
    }

    fun add(t: TransactionEntity) {

    }
}