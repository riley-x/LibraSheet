package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.Institution
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.rangeBetween
import com.example.librasheet.ui.theme.randomColor
import kotlinx.coroutines.*


const val dragGroupAll = "all"
const val dragGroupAssets = "assets"
const val dragGroupLiabilities = "liabilities"

class AccountModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.accountDao()

    /** A list of all accounts and their current balances. This is used throughout the app.
      * WARNING! Do not store pointers to accounts, since we copy the data classes **/
    val all = mutableStateListOf<Account>()
    val assets = mutableStateListOf<Account>()
    val liabilities = mutableStateListOf<Account>()

    fun load(): Job {
        return viewModel.viewModelScope.launch {
            val new = withContext(Dispatchers.IO) {
                dao.getAccounts()
            }
            all.clear()
            assets.clear()
            liabilities.clear()
            new.forEach {
                all.add(it)
                if (it.balance >= 0) assets.add(it)
                else liabilities.add(it)
                Log.d("Libra/AccountModel/load", "$it")
            }
        }
    }


    @Callback
    fun update(index: Int, name: String, institution: Institution) {
        if (name == all[index].name && institution == all[index].institution) return
        all[index] = all[index].copy(
            name = name,
            institution = institution,
        )
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.update(all[index])
        }
    }

    @Callback
    fun add(name: String, institution: Institution) {
        val accountWithoutKey = Account(
            name = name,
            color = randomColor(),
            listIndex = all.size,
            institution = institution,
        )
        viewModel.viewModelScope.launch {
            // TODO loading indicator
            all.add(accountWithoutKey.copy(
                key = withContext(Dispatchers.IO) { dao.add(accountWithoutKey) }
            ))
        }
    }

    @Callback
    fun reorder(group: String, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return

        /** Reorder the state lists to reflect UI change immediately **/
        val list = when (group) {
            dragGroupAssets -> assets
            dragGroupLiabilities -> liabilities
            else -> all
        }
        var startIndexAll = startIndex
        var endIndexAll = endIndex
        if (list != all) {
            startIndexAll = all.indexOf(list[startIndex])
            endIndexAll = all.indexOf(list[endIndex])
            all.add(endIndexAll, all.removeAt(startIndexAll))
        }
        list.add(endIndex, list.removeAt(startIndex))

        /** Update the database and other dependencies **/
        val staleEntities = mutableListOf<Account>()
        for (i in rangeBetween(startIndexAll, endIndexAll)) {
            all[i] = all[i].copy(listIndex = i)
            staleEntities.add(all[i])
        }

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.update(staleEntities)
            viewModel.updateDependencies(Dependency.ACCOUNT_REORDER)
        }
    }

    @Callback
    fun getColor(name: String): Color {
        val account = all.find { it.name == name } ?: return Color.White
        return account.color
    }

    @Callback
    fun saveColor(name: String, color: Color) {
        val (index, account) = all.withIndex().find { it.value.name == name } ?: return
        all[index] = account.copy(colorLong = color.value.toLong())
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.update(all[index])
        }
        viewModel.updateDependencies(Dependency.ACCOUNT_COLOR)
    }
}