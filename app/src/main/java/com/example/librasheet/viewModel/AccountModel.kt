package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.entity.Account
import com.example.librasheet.ui.theme.randomColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.accountDao()

    val all = mutableStateListOf<Account>()
//    val current = previewAccounts.toMutableStateList()

    @Callback
    fun rename(index: Int, name: String) {
        if (name == all[index].name) return
        all[index] = all[index].copy(name = name)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.update(all[index])
        }
    }

    @Callback
    fun add(name: String) {
        val accountWithoutKey = Account(
            name = name,
            color = randomColor(),
            listIndex = all.size,
            // TODO institute
        )
        viewModel.viewModelScope.launch {
            // TODO loading indicator
            all.add(accountWithoutKey.copy(
                key = withContext(Dispatchers.IO) { dao.add(accountWithoutKey) }
            ))
        }
    }

    @Callback
    fun reorder(startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return
        all.add(endIndex, all.removeAt(startIndex))
        // TODO delete and update all affected indices (via a SQL command)
    }
}