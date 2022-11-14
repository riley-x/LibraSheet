package com.example.librasheet.data.dao

import androidx.room.*
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
}