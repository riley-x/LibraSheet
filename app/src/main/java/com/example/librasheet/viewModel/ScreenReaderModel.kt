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
    val transactions: List<TransactionWithDetails>,
    val inverted: Boolean,
)

class ScreenReaderModel(
    val viewModel: LibraViewModel,
) {
    private val ruleDao = viewModel.application.database.ruleDao()
    private val transactionDao = viewModel.application.database.transactionDao()
    private val rootCategory = viewModel.categories.data.all

    val savedParsed = mutableMapOf<String, MutableSet<ParsedTransaction>>()
    val data = mutableStateListOf<ScreenReaderAccountState>()
    private var lastLoadScreenReaderNItems = 0

    fun load() {
        if (ScreenReader.nItems.value == 0) return
        if (ScreenReader.nItems.value <= lastLoadScreenReaderNItems) return
        lastLoadScreenReaderNItems = ScreenReader.nItems.value

        // TODO loading indicator
        viewModel.viewModelScope.launch {
            val incomeRulesDeferred = async(Dispatchers.IO) { ruleDao.getIncomeRules() }
            val expenseRulesDeferred = async(Dispatchers.IO) { ruleDao.getExpenseRules() }
            val incomeRules = incomeRulesDeferred.await()
            val expenseRules = expenseRulesDeferred.await()

            val accountStates = withContext(Dispatchers.IO) {

                val categoryMap = rootCategory.getKeyMap().also { it[ignoreKey] = Category.Ignore }
                val accountStates = mutableListOf<ScreenReaderAccountState>()

                ScreenReader.cache.forEach { (account, set) ->
                    savedParsed.getOrPut(account) { mutableSetOf() }.addAll(set)
                }
                savedParsed.mapTo(accountStates) { (accountName, transactions) ->
                    createAccountState(accountName, transactions, categoryMap, incomeRules, expenseRules)
                }
                accountStates
            }

            data.clear()
            data.addAll(accountStates)
        }
    }


    private fun createAccountState(
        accountName: String,
        parsed: Set<ParsedTransaction>,
        categoryMap: MutableMap<Long, Category>,
        incomeRules: List<CategoryRule>,
        expenseRules: List<CategoryRule>,
        account: Account? = viewModel.accounts.all.find { it.name == accountName },
        inverted: Boolean = account?.institution?.invertScreenReader ?: false,
    ): ScreenReaderAccountState {
        val transactions = mutableListOf<TransactionWithDetails>()
        for (parsedTransaction in parsed) {
            val value = if (inverted) -parsedTransaction.value else parsedTransaction.value

            // Rough duplicate protection
            if (transactionDao.count(account?.key, parsedTransaction.date, value) > 0) continue

            val rules = if (value > 0) incomeRules else expenseRules
            val rule = rules.find { it.pattern in parsedTransaction.name }
            val category = rule?.let { categoryMap.getOrDefault(it.categoryKey, null) } ?: Category.None

            val t = TransactionEntity(
                name = parsedTransaction.name,
                date = parsedTransaction.date,
                value = value,
                category = category,
                categoryKey = category.key,
                // we update account key on save
            )
            transactions.add(TransactionWithDetails(t))
        }

        return ScreenReaderAccountState(
            parsedAccountName = if (account == null) accountName else "",
            account = account,
            inverted = inverted,
            transactions = transactions.sortedByDescending { it.transaction.date },
        )
    }

    @Callback
    fun setAccount(i: Int, account: Account?) {
        data[i] = data[i].copy(account = account)
    }

    @Callback
    fun clear() {
        ScreenReader.reset()
        savedParsed.clear()
        data.clear()
    }

    @Callback
    fun loadDetail(iAccount: Int, iTransaction: Int) {
        val model = TransactionDetailModel { new, old ->
            val newList = data[iAccount].transactions.toMutableList()
            newList[iTransaction] = new
            data[iAccount] = data[iAccount].copy(transactions = newList)
            true
        }
        model.load(
            account = data[iAccount].account,
            t = data[iAccount].transactions[iTransaction],
        )
        viewModel.transactionDetails[SettingsTransactionKeyBase] = model
    }

    @Callback
    fun invert(iAccount: Int, newValue: Boolean) {
        viewModel.viewModelScope.launch {
            val incomeRulesDeferred = async(Dispatchers.IO) { ruleDao.getIncomeRules() }
            val expenseRulesDeferred = async(Dispatchers.IO) { ruleDao.getExpenseRules() }
            val incomeRules = incomeRulesDeferred.await()
            val expenseRules = expenseRulesDeferred.await()

            val accountState = withContext(Dispatchers.IO) {
                val categoryMap = rootCategory.getKeyMap().also { it[ignoreKey] = Category.Ignore }
                createAccountState(
                    accountName = data[iAccount].parsedAccountName,
                    account = data[iAccount].account,
                    inverted = newValue,
                    parsed = savedParsed.toList()[iAccount].second,
                    categoryMap = categoryMap,
                    incomeRules = incomeRules,
                    expenseRules = expenseRules,
                )
            }
            data[iAccount] = accountState
        }
    }

    @Callback
    fun save() {
        viewModel.viewModelScope.launch {
            val cachedData = data.toList()
            withContext(Dispatchers.IO) {
                cachedData.forEach { state ->
                    state.transactions.forEach { t ->
                        val withAccount = t.copy(
                            transaction = t.transaction.copy(accountKey = state.account?.key ?: 0)
                        )
                        transactionDao.add(withAccount)
                    }
                }
            }
            clear()
            viewModel.updateDependencies(dependency = Dependency.TRANSACTION)
        }
    }
}