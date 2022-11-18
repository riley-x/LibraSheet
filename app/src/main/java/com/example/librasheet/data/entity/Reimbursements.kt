package com.example.librasheet.data.entity


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation


const val reimbursementTable = "reimbursements"

@Entity(
    primaryKeys = ["expenseId", "incomeId"],
    tableName = reimbursementTable,
)
data class Reimbursement(
    val expenseId: Long,
    val incomeId: Long
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