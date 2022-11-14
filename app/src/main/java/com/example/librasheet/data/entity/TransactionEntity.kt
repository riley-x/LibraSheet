package com.example.librasheet.data.entity

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

const val transactionTable = "transaction_table"

/**
 * Make sure not to mix up names with SQL/Room Transaction.
 */
@Immutable
@Entity(
    tableName = transactionTable
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val key: Long,
    @NonNull val name: String,
    @ColumnInfo(index = true) val date: Int,
    @ColumnInfo(index = true) val accountKey: Long,
    @ColumnInfo(index = true) val categoryKey: Long,
    val value: Long,
    val valueAfterReimbursements: Long,
) {
    @Ignore var category = Category.None

    constructor(
        name: String = "",
        date: Int = 0,
        value: Long = 0,
        category: Category = Category.None,
        key: Long = 0,
        valueAfterReimbursements: Long = value,
        accountKey: Long = 0,
        categoryKey: Long = 0,
    ) : this(
        name = name,
        date = date,
        value = value,
        key = key,
        valueAfterReimbursements = valueAfterReimbursements,
        accountKey = accountKey,
        categoryKey = categoryKey,
    ) {
        this.category = category
    }
}