package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.AccountHistory
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.rangeBetween
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.theme.randomColor
import com.example.librasheet.viewModel.preview.previewAccount
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewStackedLineGraph
import com.example.librasheet.viewModel.preview.testAccountHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.accountDao()

    val all = mutableStateListOf<Account>()
    var history: MutableList<MutableList<AccountHistory>> = mutableListOf()
//    val current = previewAccounts.toMutableStateList()

    fun load() = viewModel.viewModelScope.launch {
        val (accounts, hist) = withContext(Dispatchers.IO) {
            val (accounts, hist) = dao.load()
            val accountsWithLatestBalance = accounts.mapIndexed { index, account ->
                account.copy(value = hist[index].lastOrNull()?.balance?.toFloatDollar() ?: 0f)
            }
            Pair(accountsWithLatestBalance, hist)
        }
        history = hist.toMutableList()
        all.addAll(accounts)

        all.forEach {
            Log.d("Libra/AccountModel/load", "$it")
        }
    }


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

        val staleEntities = mutableListOf<Account>()
        for (i in rangeBetween(startIndex, endIndex)) {
            all[i].listIndex = i
            staleEntities.add(all[i])
        }

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.update(all.slice(startIndex..endIndex))
        }
    }
}