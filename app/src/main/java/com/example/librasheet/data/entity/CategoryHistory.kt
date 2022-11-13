package com.example.librasheet.data.entity

import androidx.room.Entity


const val categoryHistoryTable = "category_history"


@Entity(
    tableName = categoryHistoryTable,
    primaryKeys = ["accountKey", "categoryKey", "date", "value"],
)
data class CategoryHistory(
    val accountKey: Long,
    val categoryKey: Long,
    val date: Int,
    val value: Long,
)