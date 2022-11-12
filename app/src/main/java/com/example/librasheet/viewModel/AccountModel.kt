package com.example.librasheet.viewModel

import androidx.compose.runtime.toMutableStateList
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.preview.previewAccounts

class AccountModel(
    private val viewModel: LibraViewModel,
) {
    //val current = mutableStateListOf<Account>()
    val current = previewAccounts.toMutableStateList()

    @Callback
    fun rename(index: Int, name: String) {
        if (name == current[index].name) return
        current[index] = current[index].copy(name = name)
        // TODO Room update. Only do a partial update since we don't keep track listIndex
    }

    @Callback
    fun add(name: String) {
        val account = Account(
            name = name,
            color = randomColor(),
            balance = 0,
        )
        current.add(account)
        // TODO Room update. Need wrapper insert function that finds the current last Index.
    }

    @Callback
    fun reorder(startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return
        current.add(endIndex, current.removeAt(startIndex))
        // TODO delete and update all affected indices (via a SQL command)
    }
}