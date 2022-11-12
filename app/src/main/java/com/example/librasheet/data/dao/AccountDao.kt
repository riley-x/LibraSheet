package com.example.librasheet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.AccountHistory
import com.example.librasheet.data.entity.accountHistoryTable
import com.example.librasheet.data.entity.accountTable

@Dao
interface AccountDao {
    @Insert fun add(account: Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(accountHistory: AccountHistory)

    @Query("SELECT * FROM $accountTable ORDER BY listIndex")
    fun getAccounts(): List<Account>

    @Query("SELECT * FROM $accountHistoryTable WHERE accountKey = :accountKey ORDER BY date")
    fun getHistory(accountKey: Long): List<AccountHistory>
    fun getHistory(account: Account) = getHistory(account.key)
}