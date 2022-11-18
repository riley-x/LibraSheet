package com.example.librasheet.data.entity


import androidx.room.*


const val reimbursementTable = "reimbursements"

@Entity(
    primaryKeys = ["expenseId", "incomeId"],
    tableName = reimbursementTable,
)
data class Reimbursement(
    val expenseId: Long,
    val incomeId: Long,
    val value: Long,
    // No list index because there could be multiple "lists" for each reimbursement
)


data class ReimbursementWithValue (
    @Embedded val transaction: TransactionEntity,
    @ColumnInfo(name = "reimbursedValue") val value: Long,
)


interface TransactionWithReimbursements {
    val parent: TransactionEntity
    val reimbursements: List<TransactionEntity>
}

data class ExpenseWithReimbursements(
    @Embedded override val parent: TransactionEntity,
    @Relation(
        parentColumn = "expenseId",
        entityColumn = "key",
        associateBy = Junction(Reimbursement::class)
    )
    override val reimbursements: List<TransactionEntity>
) : TransactionWithReimbursements


data class IncomeWithReimbursements(
    @Embedded override val parent: TransactionEntity,
    @Relation(
        parentColumn = "incomeId",
        entityColumn = "key",
        associateBy = Junction(Reimbursement::class)
    )
    override val reimbursements: List<TransactionEntity>
) : TransactionWithReimbursements