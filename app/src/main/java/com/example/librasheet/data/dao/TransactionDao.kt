package com.example.librasheet.data.dao

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.setDay
import com.example.librasheet.data.thisMonthEnd


@Dao
interface TransactionDao {

    @Insert fun insert(t: TransactionEntity)
    @Delete fun delete(t: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addBalanceEntry(accountHistory: AccountHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addCategoryEntry(categoryHistory: CategoryHistory)

    @Query("UPDATE $accountTable SET balance = balance + :value WHERE `key` = :account")
    fun updateBalance(account: Long, value: Long)

    @Query("UPDATE $accountHistoryTable SET balance = balance + :value WHERE accountKey = :account AND date >= :startDate")
    fun updateBalanceHistory(account: Long, startDate: Int, value: Long)

    @Query("UPDATE $categoryHistoryTable SET value = value + :value " +
            "WHERE accountKey = :account AND categoryKey = :category AND date = :date")
    fun updateCategoryHistory(account: Long, category: Long, date: Int, value: Long)

    @Transaction
    fun add(transaction: TransactionEntity) {
        val t = if (transaction.categoryKey == 0L) transaction.copy(
            categoryKey = if (transaction.value > 0) incomeKey else expenseKey
        ) else transaction

        insert(t)
        if (t.accountKey <= 0) return

        val month = thisMonthEnd(t.date)
        addBalanceEntry(AccountHistory(
            accountKey = t.accountKey,
            date = month,
            balance = 0, // we'll update below
        ))
        updateBalance(t.accountKey, t.valueAfterReimbursements)
        updateBalanceHistory(t.accountKey, month, t.valueAfterReimbursements)

        if (t.categoryKey == ignoreKey) return // we want to still measure uncategorized transactions
        addCategoryEntry(CategoryHistory(
            accountKey = t.accountKey,
            categoryKey = t.categoryKey,
            date = month,
            value = 0, // we'll update below
        ))
        updateCategoryHistory(t.accountKey, t.categoryKey, month, t.valueAfterReimbursements)
    }

    @Transaction
    fun add(list: List<TransactionEntity>) {
        list.forEach { add(it) }
    }

    @Transaction
    fun undo(t: TransactionEntity) {
        delete(t)
        val month = thisMonthEnd(t.date)
        updateBalance(t.accountKey, -t.valueAfterReimbursements)
        updateBalanceHistory(t.accountKey, month, -t.valueAfterReimbursements)
        updateCategoryHistory(t.accountKey, t.categoryKey, month, -t.valueAfterReimbursements)
    }

    @Transaction
    fun update(new: TransactionEntity, old: TransactionEntity) {
        undo(old)
        add(new)
    }

    @RawQuery
    fun get(q: SimpleSQLiteQuery): List<TransactionEntity>

    fun get(filters: TransactionFilters): List<TransactionEntity> = get(getTransactionFilteredQuery(filters))

    @Query("SELECT * FROM $transactionTable t JOIN $reimbursementTable r ON t.`key` = r.expenseId WHERE r.incomeId = :incomeKey")
    fun getIncomeReimbursements(incomeKey: Long): List<TransactionEntity>

    @Query("SELECT * FROM $transactionTable t JOIN $reimbursementTable r ON t.`key` = r.incomeId WHERE r.expenseId = :expenseKey")
    fun getExpenseReimbursements(expenseKey: Long): List<TransactionEntity>

    @Query("SELECT * FROM $allocationTable WHERE transactionKey = :key ORDER BY listIndex")
    fun getAllocations(key: Long): List<Allocation>

    @Transaction
    fun getDetails(transaction: TransactionEntity): Pair<List<TransactionEntity>, List<Allocation>> {
        val reimbursements =
            if (transaction.value > 0) getIncomeReimbursements(transaction.key)
            else getExpenseReimbursements(transaction.key)
        val allocations = getAllocations(transaction.key)
        return Pair(reimbursements, allocations)
    }
}

@Immutable
data class TransactionFilters(
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val startDate: Int? = null,
    val endDate: Int? = null,
    val account: Long? = null,
    val category: Category? = null,
    val limit: Int? = null,
)

fun getTransactionFilteredQuery(filter: TransactionFilters): SimpleSQLiteQuery {
    val args = mutableListOf<Any>()
    var q = "SELECT * FROM $transactionTable"
    if (filter.minValue != null) {
        q += " WHERE value >= ?"
        args.add(filter.minValue)
    }
    if (filter.maxValue != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " value <= ?"
        args.add(filter.maxValue)
    }
    if (filter.startDate != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " date >= ?"
        args.add(filter.startDate)
    }
    if (filter.endDate != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " date <= ?"
        args.add(filter.endDate)
    }
    if (filter.account != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " accountKey = ?"
        args.add(filter.account)
    }
    if (filter.category != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " categoryKey = ?"
        args.add(filter.category.key)
    }
    q += " ORDER BY date DESC"
    if (filter.limit != null) {
        q += " LIMIT ?"
        args.add(filter.limit)
    }
    Log.i("Libra/TransactionDao/getTransactionFilteredQuery", q)
    return SimpleSQLiteQuery(q, args.toTypedArray())
}