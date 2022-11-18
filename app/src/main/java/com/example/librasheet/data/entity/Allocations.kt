package com.example.librasheet.data.entity

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


const val allocationTable = "allocations"


@Immutable
@Entity(
    tableName = allocationTable
)
data class Allocation(
    @PrimaryKey(autoGenerate = true) val key: Long,
    @NonNull val name: String,
    @ColumnInfo(index = true) val transactionKey: Long,
    val categoryKey: Long,
    val value: Long,
    var listIndex: Int, // this is not used by compose, so safe to be a var
) {
    @Ignore var category = Category.None
}