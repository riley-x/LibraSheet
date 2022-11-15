package com.example.librasheet.data.dao

import androidx.compose.runtime.Immutable
import androidx.room.*
import com.example.librasheet.data.entity.CategoryHistory
import com.example.librasheet.data.entity.categoryHistoryTable


@Immutable
data class TimeSeries (
    val date: Int,
    val value: Long,
)

@Dao
interface CategoryHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(it: CategoryHistory)

    @Query("SELECT date, SUM(value) as value FROM $categoryHistoryTable GROUP BY date ORDER BY date")
    fun getNetIncome(): List<TimeSeries>

    @MapInfo(keyColumn = "categoryKey", valueColumn = "average")
    @Query("SELECT categoryKey, AVG(sums) as average FROM (" +
            "SELECT categoryKey, date, SUM(value) as sums " +
            "FROM $categoryHistoryTable WHERE date >= :startDate AND date <= :endDate GROUP BY categoryKey, date" +
            ") GROUP BY categoryKey")
    fun getAverages(startDate: Int, endDate: Int): Map<Long, Long>

    /** The current month might not be complete yet, and if so the returned average is misleading. **/
    @MapInfo(keyColumn = "categoryKey", valueColumn = "average")
    @Query("SELECT categoryKey, AVG(sums) as average FROM (" +
            "SELECT categoryKey, date, SUM(value) as sums " +
            "FROM $categoryHistoryTable GROUP BY categoryKey, date" +
            ") GROUP BY categoryKey")
    fun getAverages(): Map<Long, Long>

    @MapInfo(keyColumn = "categoryKey", valueColumn = "sums")
    @Query("SELECT categoryKey, SUM(value) as sums " +
            "FROM $categoryHistoryTable WHERE date = :date GROUP BY categoryKey"
            )
    fun getDate(date: Int): Map<Long, Long>

    @Query("SELECT accountKey, categoryKey, date, SUM(value) as value FROM $categoryHistoryTable GROUP BY date, categoryKey ORDER BY date")
    fun getAll(): List<CategoryHistory>
}