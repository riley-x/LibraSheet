package com.example.librasheet.data.dao

import androidx.room.*
import com.example.librasheet.data.entity.*


@Dao
interface AccountDao {
    @Insert fun add(account: Account): Long
    @Update fun update(account: Account)
    @Update fun update(accounts: List<Account>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(accountHistory: AccountHistory)

    @Query("SELECT * FROM $accountTable ORDER BY listIndex")
    fun getAccounts(): List<Account>

    @Query("SELECT * FROM $accountHistoryTable ORDER BY date")
    fun getHistory(): List<AccountHistory>

    @Query("SELECT * FROM $accountHistoryTable WHERE accountKey = :accountKey ORDER BY date")
    fun getHistory(accountKey: Long): List<AccountHistory>
    fun getHistory(account: Account) = getHistory(account.key)
}