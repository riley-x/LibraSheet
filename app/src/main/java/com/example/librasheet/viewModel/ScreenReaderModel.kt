package com.example.librasheet.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.dao.TransactionWithDetails
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.entity.ignoreKey
import com.example.librasheet.screenReader.ParsedTransaction
import com.example.librasheet.screenReader.ScreenReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Immutable
data class ScreenReaderAccountState(
    val account: Account?,
    val parsedAccountName: String,
    val transactions: List<TransactionEntity>,
    val inverted: Boolean,
)



class ScreenReaderModel(
    val viewModel: LibraViewModel,
) {
    private val ruleDao = viewModel.application.database.ruleDao()
    private val rootCategory = viewModel.categories.data.all

    val data = mutableStateListOf<ScreenReaderAccountState>()

    fun load() {
        if (ScreenReader.nItems.value == 0) return

        // TODO loading indicator
        viewModel.viewModelScope.launch {
            val incomeRulesDeferred = async(Dispatchers.IO) { ruleDao.getIncomeRules() }
            val expenseRulesDeferred = async(Dispatchers.IO) { ruleDao.getExpenseRules() }
            val incomeRules = incomeRulesDeferred.await()
            val expenseRules = expenseRulesDeferred.await()

            val accountStates = withContext(Dispatchers.Main) {

                val categoryMap = rootCategory.getKeyMap().also { it[ignoreKey] = Category.Ignore }
                val accountStates = mutableListOf<ScreenReaderAccountState>()

                ScreenReader.cache.mapTo(accountStates) { (accountName, transactions) ->
                    createAccountState(accountName, transactions, categoryMap, incomeRules, expenseRules)
                }
                ScreenReader.reset()

                accountStates
            }

            data.clear() // TODO should append not clear
            data.addAll(accountStates)
        }
    }

    private fun createAccountState(
        accountName: String,
        parsed: Set<ParsedTransaction>,
        categoryMap: MutableMap<Long, Category>,
        incomeRules: List<CategoryRule>,
        expenseRules: List<CategoryRule>
    ): ScreenReaderAccountState {
        val account = viewModel.accounts.all.find { it.name == accountName }
        val inverted = account?.institution?.invertScreenReader ?: false
        val transactions = mutableListOf<TransactionEntity>()

        for (parsedTransaction in parsed) {
            val value = if (inverted) -parsedTransaction.value else parsedTransaction.value
            // TODO duplicate removal

            val rules = if (value > 0) incomeRules else expenseRules
            val rule = rules.find { it.pattern in parsedTransaction.name }
            val category = rule?.let { categoryMap.getOrDefault(it.categoryKey, null) } ?: Category.None

            val t = TransactionEntity(
                name = parsedTransaction.name,
                date = parsedTransaction.date,
                value = value,
                category = category,
                categoryKey = category.key,
                // TODO account key on save
            )
            transactions.add(t)
        }

        return ScreenReaderAccountState(
            parsedAccountName = if (account == null) accountName else "",
            account = account,
            inverted = inverted,
            transactions = transactions,
        )
    }

    @Callback
    fun setAccount(i: Int, account: Account?) {
        data[i] = data[i].copy(account = account)
    }

    @Callback
    fun clear() = data.clear()

    @Callback
    fun loadDetail(iAccount: Int, iTransaction: Int) {
        viewModel.transactionDetails.add(
            TransactionDetailModel { new, old ->
                val newList = data[iAccount].transactions.toMutableList()
                newList[iTransaction] = new.transaction // TODO change to with details
                data[iAccount] = data[iAccount].copy(transactions = newList)
                true
            }
        )
        viewModel.transactionDetails.last().load(
            account = data[iAccount].account,
            t = TransactionWithDetails(
                transaction = data[iAccount].transactions[iTransaction],
                reimbursements = emptyList(),
                allocations = emptyList(),
            )
        )
    }
}