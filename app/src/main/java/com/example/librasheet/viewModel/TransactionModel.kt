package com.example.librasheet.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.data.setDay
import com.example.librasheet.data.toIntDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Immutable
data class TransactionFilters(
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val startDate: Int? = null,
    val endDate: Int? = null,
    val account: Account? = null,
    val category: Category? = null,
)



class TransactionModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.transactionDao()
    private val defaultFilter = TransactionFilters(
        startDate = Calendar.getInstance().toIntDate().setDay(0)
    )

    /** Current list to show in settings tab **/
    val settingsList = mutableStateListOf<TransactionEntity>()
    val settingsFilter = mutableStateOf(defaultFilter)

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

    @Callback
    fun loadSettings() {

    }
}