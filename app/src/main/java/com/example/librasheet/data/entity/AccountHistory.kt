package com.example.librasheet.data.entity

import androidx.room.Entity
import com.example.librasheet.data.HistoryEntry

const val accountHistoryTable = "account_history"

@Entity(
    tableName = accountHistoryTable,
    primaryKeys = ["accountKey", "date"],
)
data class AccountHistory(
    val accountKey: Long,
    override val date: Int,
    val balance: Long,
): HistoryEntry {
    override val seriesKey: Long
        get() = accountKey
    override val value: Long
        get() = balance
}

