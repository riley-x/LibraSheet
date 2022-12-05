package com.example.librasheet.viewModel.preview

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.screenReader.ScreenReader


@Immutable
data class ScreenReaderAccountState(
    val account: Account?,
    val parsedAccountName: String,
    val transactions: List<TransactionEntity>,
    val inverted: Boolean,
)



class ScreenReaderModel(
    val accounts: SnapshotStateList<Account>,
) {
    val data = mutableStateListOf<ScreenReaderAccountState>()

    fun load() {
        data.clear()
        ScreenReader.cache.mapTo(data) { (accountName, transactions) ->
            val account = accounts.find { it.name == accountName }
            ScreenReaderAccountState(
                parsedAccountName = if (account == null) accountName else "",
                account = account,
                inverted = account?.institution?.invertScreenReader ?: false,
                transactions = transactions.map {
                    TransactionEntity(
                        name = it.name,
                        date = it.date,
                        value = it.value,
                        // TODO category
                        // TODO account key on save
                    )
                }
            )
        }
        ScreenReader.reset()
    }
}