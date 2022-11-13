package com.example.librasheet.data.dao

import androidx.compose.runtime.Immutable
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}