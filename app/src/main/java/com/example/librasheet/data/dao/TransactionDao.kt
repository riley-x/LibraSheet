package com.example.librasheet.data.dao

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.thisMonthEnd
import kotlin.math.exp


@Dao
interface TransactionDao {

    @Insert fun insert(t: TransactionEntity): Long
    @Delete fun delete(t: TransactionEntity)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addCategoryEntry(categoryHistory: CategoryHistory)

    @Query("UPDATE $accountTable SET balance = balance + :value WHERE `key` = :account")
    fun updateBalance(account: Long, value: Long)

    @Query("UPDATE $categoryHistoryTable SET value = value + :value " +
            "WHERE accountKey = :account AND categoryKey = :category AND date = :date")
    fun updateCategoryHistory(account: Long, category: Long, date: Int, value: Long)

    @Transaction
    fun add(transaction: TransactionEntity): Long {
        Log.d("Libra/TransactionDao/add", "$transaction")
        val t = if (transaction.categoryKey == 0L) transaction.copy(
            categoryKey = if (transaction.value > 0) incomeKey else expenseKey
        ) else transaction

        val newKey = insert(t)
        if (t.accountKey <= 0) return newKey

        val month = thisMonthEnd(t.date)

        updateBalance(t.accountKey, t.value)
        addCategoryEntry(CategoryHistory(
            accountKey = t.accountKey,
            categoryKey = t.categoryKey,
            date = month,
            value = 0, // we'll update below
        ))
        updateCategoryHistory(t.accountKey, t.categoryKey, month, t.valueAfterReimbursements)
        // this should use the reimbursed value since category history is used for income/expenses.
        // But the account balance ones above should use the normal value.

        return newKey
    }

    @Transaction
    fun add(list: List<TransactionEntity>) {
        list.forEach { add(it) }
    }

    @Transaction
    fun undo(t: TransactionEntity) {
        Log.d("Libra/TransactionDao/undo", "$t")
        delete(t)
        val month = thisMonthEnd(t.date)

        if (t.accountKey <= 0) return
        updateBalance(t.accountKey, -t.value)
        updateCategoryHistory(t.accountKey, t.categoryKey, month, -t.valueAfterReimbursements)
    }

    @Transaction
    fun update(new: TransactionEntity, old: TransactionEntity) {
        undo(old)
        add(new)
    }

    @RawQuery
    fun get(q: SimpleSQLiteQuery): List<TransactionEntity>

    fun get(filters: TransactionFilters): List<TransactionEntity> = get(getTransactionFilteredQuery(filters))

    @Query("SELECT $transactionFields, r.value as reimbursedValue FROM $transactionTable t JOIN $reimbursementTable r ON t.`key` = r.expenseId WHERE r.incomeId = :incomeKey")
    fun getIncomeReimbursements(incomeKey: Long): List<ReimbursementWithValue>

    @Query("SELECT $transactionFields, r.value as reimbursedValue FROM $transactionTable t JOIN $reimbursementTable r ON t.`key` = r.incomeId WHERE r.expenseId = :expenseKey")
    fun getExpenseReimbursements(expenseKey: Long): List<ReimbursementWithValue>

    @Query("SELECT * FROM $allocationTable WHERE transactionKey = :key ORDER BY listIndex")
    fun getAllocations(key: Long): List<Allocation>

    @Transaction
    fun getDetails(transaction: TransactionEntity): Pair<List<ReimbursementWithValue>, List<Allocation>> {
        val reimbursements =
            if (transaction.value > 0) getIncomeReimbursements(transaction.key)
            else getExpenseReimbursements(transaction.key)
        val allocations = getAllocations(transaction.key)
        return Pair(reimbursements, allocations)
    }

    @Insert fun insert(x: Reimbursement)
    @Update fun update(x: Reimbursement)
    @Delete fun delete(x: Reimbursement)

    @Insert fun insert(x: Allocation)
    @Update fun update(x: Allocation)
    @Delete fun delete(x: Allocation)

    /** Value should always be positive **/
    @Transaction
    fun addReimbursement(t1: TransactionEntity, t2: TransactionEntity, value: Long): Pair<TransactionEntity, TransactionEntity> {
        Log.d("Libra/TransactionDao/addReimbursement", "1: $t1")
        Log.d("Libra/TransactionDao/addReimbursement", "2: $t2")
        val expense = if (t1.value > 0) t2 else t1
        val income = if (t1.value > 0) t1 else t2

        val reimbursement = Reimbursement(
            expenseId = expense.key,
            incomeId = income.key,
            value = value
        )
        val newIncome = income.copy(
            valueAfterReimbursements = income.valueAfterReimbursements - value
        )
        val newExpense = expense.copy(
            valueAfterReimbursements = expense.valueAfterReimbursements + value
        )
        insert(reimbursement)
        update(newIncome, income)
        update(newExpense, expense)

        val new1 = if (t1.value > 0) newIncome else newExpense
        val new2 = if (t1.value > 0) newExpense else newIncome
        return Pair(new1, new2)
    }

    /** Value should always be positive **/
    @Transaction
    fun deleteReimbursement(t1: TransactionEntity, t2: TransactionEntity, value: Long): Pair<TransactionEntity, TransactionEntity> {
        Log.d("Libra/TransactionDao/deleteReimbursement", "1: $t1")
        Log.d("Libra/TransactionDao/deleteReimbursement", "2: $t2")
        val expense = if (t1.value > 0) t2 else t1
        val income = if (t1.value > 0) t1 else t2

        val reimbursement = Reimbursement(
            expenseId = expense.key,
            incomeId = income.key,
            value = 0
        )
        val newIncome = income.copy(
            valueAfterReimbursements = income.valueAfterReimbursements + value
        )
        val newExpense = expense.copy(
            valueAfterReimbursements = expense.valueAfterReimbursements - value
        )
        delete(reimbursement)
        update(newIncome, income)
        update(newExpense, expense)

        val new1 = if (t1.value > 0) newIncome else newExpense
        val new2 = if (t1.value > 0) newExpense else newIncome
        return Pair(new1, new2)
    }


    /** Value should always be positive **/
    @Transaction
    fun addAllocation(t: TransactionEntity, allocation: Allocation): TransactionEntity {
        Log.d("Libra/TransactionDao/addAllocation", "t: $t")
        Log.d("Libra/TransactionDao/addAllocation", "a: $allocation")
        val isIncome = t.value > 0

        val newTransaction = t.copy(
            valueAfterReimbursements = t.valueAfterReimbursements - (if (isIncome) 1 else -1) * allocation.value
        )
        update(newTransaction, t)
        insert(allocation)

        if (t.accountKey <= 0) return newTransaction

        val month = thisMonthEnd(t.date)
        addCategoryEntry(CategoryHistory(
            accountKey = t.accountKey,
            categoryKey = allocation.categoryKey,
            date = month,
            value = 0, // we'll update below
        ))
        updateCategoryHistory(t.accountKey, allocation.categoryKey, month, (if (isIncome) 1 else -1) * allocation.value)
        return newTransaction
    }

    /** Value should always be positive **/
    @Transaction
    fun removeAllocation(t: TransactionEntity, allocation: Allocation): TransactionEntity {
        Log.d("Libra/TransactionDao/removeAllocation", "t: $t")
        Log.d("Libra/TransactionDao/removeAllocation", "a: $allocation")
        val isIncome = t.value > 0

        val newTransaction = t.copy(
            valueAfterReimbursements = t.valueAfterReimbursements + (if (isIncome) 1 else -1) * allocation.value
        )
        update(newTransaction, t)
        delete(allocation)

        if (t.accountKey <= 0) return newTransaction

        val month = thisMonthEnd(t.date)
        updateCategoryHistory(t.accountKey, allocation.categoryKey, month, (if (isIncome) -1 else 1) * allocation.value)
        return newTransaction
    }


    @Transaction
    fun add(t: TransactionWithDetails) {
        val key = add(t.transaction)
        Log.d("Libra/TransactionDao/add", "oldKey=${t.transaction.key} newKey=$key")
        var trans = t.transaction.copy(key = key)
        t.reimbursements.forEach {
            trans = addReimbursement(trans, it.transaction, it.value).first
        }
        t.allocations.forEach {
            trans = addAllocation(trans, it.copy(
                transactionKey = key
            ))
        }
    }

    @Transaction
    fun undo(t: TransactionWithDetails) {
        var trans = t.transaction
        t.allocations.forEach {
            trans = removeAllocation(trans, it)
        }
        t.reimbursements.forEach {
            trans = deleteReimbursement(trans, it.transaction, it.value).first
        }
        undo(trans)
    }

    @Transaction
    fun update(new: TransactionWithDetails, old: TransactionWithDetails) {
        undo(old)
        add(new)
    }
}

@Immutable
data class TransactionFilters(
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val startDate: Int? = null,
    val endDate: Int? = null,
    val account: Long? = null,
    val category: Category? = null,
    val limit: Int? = null,
)

fun getTransactionFilteredQuery(filter: TransactionFilters): SimpleSQLiteQuery {
    val args = mutableListOf<Any>()
    var q = "SELECT * FROM $transactionTable"
    if (filter.minValue != null) {
        q += " WHERE value >= ?"
        args.add(filter.minValue)
    }
    if (filter.maxValue != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " value <= ?"
        args.add(filter.maxValue)
    }
    if (filter.startDate != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " date >= ?"
        args.add(filter.startDate)
    }
    if (filter.endDate != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " date <= ?"
        args.add(filter.endDate)
    }
    if (filter.account != null) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " accountKey = ?"
        args.add(filter.account)
    }
    if (filter.category != null && filter.category.key != 0L) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " categoryKey = ?"
        args.add(filter.category.key)
    }
    q += " ORDER BY date DESC"
    if (filter.limit != null) {
        q += " LIMIT ?"
        args.add(filter.limit)
    }
    Log.i("Libra/TransactionDao/getTransactionFilteredQuery", q)
    return SimpleSQLiteQuery(q, args.toTypedArray())
}

@Immutable
data class TransactionWithDetails(
    val transaction: TransactionEntity,
    val reimbursements: List<ReimbursementWithValue>,
    val allocations: List<Allocation>
)
