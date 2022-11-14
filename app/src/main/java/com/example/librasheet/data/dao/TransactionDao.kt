package com.example.librasheet.data.dao

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.setDay


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
            "WHERE accountKey = :account AND categoryKey = :category AND date >= :startDate")
    fun updateCategoryHistory(account: Long, category: Long, startDate: Int, value: Long)

    @Transaction
    fun add(t: TransactionEntity) {
        insert(t)
        if (t.accountKey <= 0) return
        addBalanceEntry(AccountHistory(
            accountKey = t.accountKey,
            date = t.date.setDay(0),
            balance = 0, // we'll update below
        ))
        updateBalance(t.accountKey, t.valueAfterReimbursements)
        updateBalanceHistory(t.accountKey, t.date, t.valueAfterReimbursements)

        if (t.categoryKey < 0) return // we want to still measure uncategorized transactions
        addCategoryEntry(CategoryHistory(
            accountKey = t.accountKey,
            categoryKey = t.categoryKey,
            date = t.date.setDay(0),
            value = 0, // we'll update below
        ))
        updateCategoryHistory(t.accountKey, t.categoryKey, t.date, t.valueAfterReimbursements)
    }

    @Transaction
    fun undo(t: TransactionEntity) {
        delete(t)
        updateBalance(t.accountKey, -t.valueAfterReimbursements)
        updateBalanceHistory(t.accountKey, t.date, -t.valueAfterReimbursements)
        updateCategoryHistory(t.accountKey, t.categoryKey, t.date, -t.valueAfterReimbursements)
    }

    @Transaction
    fun update(new: TransactionEntity, old: TransactionEntity) {
        undo(old)
        add(new)
    }


    @RawQuery
    fun get(q: SimpleSQLiteQuery): List<TransactionEntity>

    fun get(filters: TransactionFilters): List<TransactionEntity> = get(getTransactionFilteredQuery(filters))
}

@Immutable
data class TransactionFilters(
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val startDate: Int? = null,
    val endDate: Int? = null,
    val account: Account? = null,
    val category: Category? = null,
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
        args.add(filter.account.key)
    }
    if (filter.category != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " categoryKey = ?"
        args.add(filter.category.key)
    }
    q += " ORDER BY date DESC"
    Log.i("Libra/TransactionDao/getTransactionFilteredQuery", q)
    return SimpleSQLiteQuery(q, args.toTypedArray())
}