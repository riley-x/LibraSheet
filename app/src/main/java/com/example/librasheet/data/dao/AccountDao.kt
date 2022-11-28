package com.example.librasheet.data.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.librasheet.data.HistoryEntryBase
import com.example.librasheet.data.entity.*


@Dao
interface AccountDao {
    @Insert fun add(account: Account): Long
    @Update fun update(account: Account)
    @Update fun update(accounts: List<Account>)

    @Query("SELECT * FROM $accountTable ORDER BY listIndex")
    fun getAccounts(): List<Account>

    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
    fun checkpoint() {
        checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
    }

    @Query("SELECT accountKey as seriesKey, date, SUM(value) as value FROM $categoryHistoryTable GROUP BY date, accountKey ORDER BY date")
    fun getHistory(): List<HistoryEntryBase>

    @Query("SELECT accountKey as seriesKey, date, SUM(value) as value FROM $categoryHistoryTable WHERE accountKey = :key GROUP BY date ORDER BY date")
    fun getHistory(key: Long): List<HistoryEntryBase>
}