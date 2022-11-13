package com.example.librasheet.data.entity

import androidx.room.Entity
import com.example.librasheet.data.HistoryEntry


const val categoryHistoryTable = "category_history"


@Entity(
    tableName = categoryHistoryTable,
    primaryKeys = ["accountKey", "categoryKey", "date"],
)
data class CategoryHistory(
    val accountKey: Long,
    val categoryKey: Long,
    override val date: Int,
    override val value: Long,
): HistoryEntry {
    override val seriesKey: Long
        get() = categoryKey
}