package com.example.librasheet.data.entity

import androidx.annotation.NonNull
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.librasheet.data.Institution

const val accountTable = "account"
const val accountHistoryTable = "account_history"

@Entity(
    tableName = accountTable
)
data class Account(
    @PrimaryKey(autoGenerate = true) val key: Int = 0,
    @NonNull val name: String,
    val institution: Institution,
    val colorLong: Long,
    val listIndex: Int,
) {
    val color: Color
        get() = Color(value = colorLong.toULong())
}


@Entity(
    tableName = accountHistoryTable,
    primaryKeys = ["account", "date"],
)
data class AccountHistory(
    val account: Long,
    val date: Int,
    val balance: Long,
)