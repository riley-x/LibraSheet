package com.example.librasheet.data.dao

import androidx.room.*
import com.example.librasheet.data.entity.*


@Dao
interface AccountDao {
    @Insert fun add(account: Account): Long
    @Update fun update(account: Account)
    @Update fun update(accounts: List<Account>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(accountHistory: AccountHistoryEntity)

    @Query("SELECT * FROM $accountTable ORDER BY listIndex")
    fun getAccounts(): List<Account>

    @Query("SELECT date, balance FROM $accountHistoryTable WHERE accountKey = :accountKey ORDER BY date")
    fun getHistory(accountKey: Long): List<AccountHistory>
    fun getHistory(account: Account) = getHistory(account.key)

    @Transaction
    fun load(): Pair<List<Account>, List<MutableList<AccountHistory>>> {
        val accounts = getAccounts()
        val history = accounts.map {
            getHistory(it.key).toMutableList()
        }
        return Pair(accounts, history)
    }
}