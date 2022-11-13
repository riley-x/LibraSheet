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

    @Query("SELECT date, SUM(value) as value FROM $categoryHistoryTable GROUP BY accountKey, categoryKey ORDER BY date")
    fun getNetIncome(): List<TimeSeries>

    @MapInfo(keyColumn = "categoryKey", valueColumn = "average")
    @Query("SELECT categoryKey, AVG(sums) as average FROM (" +
            "SELECT categoryKey, date, SUM(value) as sums " +
            "FROM $categoryHistoryTable WHERE date >= :startDate GROUP BY accountKey" +
            ") GROUP BY date")
    fun getAverages(startDate: Int): Map<Long, Long>

    @MapInfo(keyColumn = "categoryKey", valueColumn = "average")
    @Query("SELECT categoryKey, AVG(sums) as average FROM (" +
            "SELECT categoryKey, date, SUM(value) as sums " +
            "FROM $categoryHistoryTable GROUP BY accountKey" +
            ") GROUP BY date")
    fun getAverages(): Map<Long, Long>
}