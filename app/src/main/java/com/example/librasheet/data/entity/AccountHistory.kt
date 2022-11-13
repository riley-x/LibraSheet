package com.example.librasheet.data.entity

import androidx.room.Entity

const val accountHistoryTable = "account_history"

@Entity(
    tableName = accountHistoryTable,
    primaryKeys = ["accountKey", "date"],
)
data class AccountHistory(
    val accountKey: Long,
    val date: Int,
    val balance: Long,
)