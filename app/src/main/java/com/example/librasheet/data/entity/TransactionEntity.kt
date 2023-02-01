package com.example.librasheet.data.entity

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

const val transactionTable = "transaction_table"
const val transactionFields = "t.`key`, t.name, t.date, t.accountKey, t.categoryKey, t.value, t.valueAfterReimbursements"

/**
 * Make sure not to mix up names with SQL/Room Transaction.
 *
 * @param valueAfterReimbursements is a cached calculation of the remaining value attributable to
 * this transaction after subtracting out reimbursements. It should only be used for UI display and
 * not trusted for database calculations, since i.e. adding reimbursements from two transactions into
 * the same target transaction will cause an update conflict. On the database side, increment
 * operations should be used instead.
 *
 * @property accountName is just used for display on transaction rows
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
    @Ignore var accountName: String = ""

    constructor(
        name: String = "",
        date: Int = 0,
        value: Long = 0,
        category: Category = Category.None,
        accountName: String = "",
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
        this.accountName = accountName
    }
}