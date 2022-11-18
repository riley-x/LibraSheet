package com.example.librasheet.viewModel.preview

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.librasheet.data.HistoryEntryBase
import com.example.librasheet.data.dao.*
import com.example.librasheet.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

internal val emptyScope = CoroutineScope(EmptyCoroutineContext)

internal class FakeCategoryDao: CategoryDao {
    override fun getIncome(): List<CategoryWithChildren> {
        TODO("Not yet implemented")
    }

    override fun getExpense(): List<CategoryWithChildren> {
        TODO("Not yet implemented")
    }

    override fun getMaxKey(): Long {
        TODO("Not yet implemented")
    }

    override fun add(category: Category): Long {
        TODO("Not yet implemented")
    }

    override fun update(category: Category) {
        TODO("Not yet implemented")
    }

    override fun update(categories: List<Category>) {
        TODO("Not yet implemented")
    }

    override fun delete(category: Category) {
        TODO("Not yet implemented")
    }

    override fun delete(categories: List<Category>) {
        TODO("Not yet implemented")
    }

    override fun unmatchRules(categoryKeys: List<Long>) {
        TODO("Not yet implemented")
    }

    override fun unmatchTransactions(categoryKeys: List<Long>) {
        TODO("Not yet implemented")
    }
}


internal class FakeHistoryDao: CategoryHistoryDao {
    override fun add(it: CategoryHistory) {
        TODO("Not yet implemented")
    }

    override fun getIncome(account: Long): List<TimeSeries> {
        TODO("Not yet implemented")
    }

    override fun getExpense(account: Long): List<TimeSeries> {
        TODO("Not yet implemented")
    }

    override fun getNetIncome(account: Long): List<TimeSeries> {
        TODO("Not yet implemented")
    }

    override fun getNetIncome(): List<TimeSeries> {
        TODO("Not yet implemented")
    }

    override fun getIncomeAndExpense(account: Long): List<HistoryEntryBase> {
        TODO("Not yet implemented")
    }

    override fun getAverages(startDate: Int, endDate: Int): Map<Long, Long> {
        TODO("Not yet implemented")
    }

    override fun getAverages(): Map<Long, Long> {
        TODO("Not yet implemented")
    }

    override fun getDate(date: Int): Map<Long, Long> {
        TODO("Not yet implemented")
    }

    override fun getAll(): List<CategoryHistory> {
        TODO("Not yet implemented")
    }

    override fun getTotals(startDate: Int): Map<Long, Long> {
        TODO("Not yet implemented")
    }
}


internal class FakeAccountDao: AccountDao {
    override fun add(account: Account): Long {
        TODO("Not yet implemented")
    }

    override fun add(accountHistory: AccountHistory) {
        TODO("Not yet implemented")
    }

    override fun update(account: Account) {
        TODO("Not yet implemented")
    }

    override fun update(accounts: List<Account>) {
        TODO("Not yet implemented")
    }

    override fun getAccounts(): List<Account> {
        TODO("Not yet implemented")
    }

    override fun getHistory(): List<AccountHistory> {
        TODO("Not yet implemented")
    }

    override fun getHistory(accountKey: Long): List<AccountHistory> {
        TODO("Not yet implemented")
    }

}


internal class FakeTransactionDao: TransactionDao {
    override fun insert(t: TransactionEntity): Long {
        TODO("Not yet implemented")
    }

    override fun insert(x: Reimbursement) {
        TODO("Not yet implemented")
    }

    override fun delete(t: TransactionEntity) {
        TODO("Not yet implemented")
    }

    override fun delete(x: Reimbursement) {
        TODO("Not yet implemented")
    }

    override fun addBalanceEntry(accountHistory: AccountHistory) {
        TODO("Not yet implemented")
    }

    override fun addCategoryEntry(categoryHistory: CategoryHistory) {
        TODO("Not yet implemented")
    }

    override fun updateBalance(account: Long, value: Long) {
        TODO("Not yet implemented")
    }

    override fun updateBalanceHistory(account: Long, startDate: Int, value: Long) {
        TODO("Not yet implemented")
    }

    override fun updateCategoryHistory(account: Long, category: Long, date: Int, value: Long) {
        TODO("Not yet implemented")
    }

    override fun update(x: Reimbursement) {
        TODO("Not yet implemented")
    }

    override fun get(q: SimpleSQLiteQuery): List<TransactionEntity> {
        TODO("Not yet implemented")
    }

    override fun getIncomeReimbursements(incomeKey: Long): List<ReimbursementWithValue> {
        TODO("Not yet implemented")
    }

    override fun getExpenseReimbursements(expenseKey: Long): List<ReimbursementWithValue> {
        TODO("Not yet implemented")
    }

    override fun getAllocations(key: Long): List<Allocation> {
        TODO("Not yet implemented")
    }
}

internal class FakeRuleDao: RuleDao {
    override fun getIndex(key: Long): Int {
        TODO("Not yet implemented")
    }

    override fun getMaxIndex(): Int {
        TODO("Not yet implemented")
    }

    override fun update(rule: CategoryRule) {
        TODO("Not yet implemented")
    }

    override fun update(rule: CategoryRuleEntity) {
        TODO("Not yet implemented")
    }

    override fun add(rule: CategoryRuleEntity): Long {
        TODO("Not yet implemented")
    }

    override fun delete(key: Long) {
        TODO("Not yet implemented")
    }

    override fun getIncomeRules(): List<CategoryRule> {
        TODO("Not yet implemented")
    }

    override fun getIncomeRules(categoryKey: Long): List<CategoryRule> {
        TODO("Not yet implemented")
    }

    override fun getExpenseRules(): List<CategoryRule> {
        TODO("Not yet implemented")
    }

    override fun getExpenseRules(categoryKey: Long): List<CategoryRule> {
        TODO("Not yet implemented")
    }

    override fun getIgnoredIncome(): List<CategoryRule> {
        TODO("Not yet implemented")
    }

    override fun getIgnoredExpense(): List<CategoryRule> {
        TODO("Not yet implemented")
    }

    override fun decrementIndices(startIndex: Int, endIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun incrementIndices(startIndex: Int, endIndex: Int) {
        TODO("Not yet implemented")
    }

}