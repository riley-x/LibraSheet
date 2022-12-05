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

    @MapInfo(keyColumn = "name", valueColumn = "date")
    @Query("SELECT $accountTable.name as name, MAX(date) as date FROM $transactionTable " +
            "INNER JOIN $accountTable ON $transactionTable.accountKey=$accountTable.`key` GROUP BY accountKey")
    fun getLastDates(): Map<String, Int>

    @Query("UPDATE $accountTable SET balance = balance + :value WHERE `key` = :account")
    fun updateBalance(account: Long, value: Long)

    /** Don't use this function, use instead [updateCategoryHistory] **/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addCategoryEntry(categoryHistory: CategoryHistory)

    /** Don't use this function, use instead [updateCategoryHistory] **/
    @Query("UPDATE $categoryHistoryTable SET value = value + :value " +
            "WHERE accountKey = :account AND categoryKey = :category AND date = :date")
    fun updateCategoryHistoryTable(account: Long, category: Long, date: Int, value: Long)

    @Transaction
    fun updateCategoryHistory(account: Long, category: Long, date: Int, value: Long) {
        /** Need to make sure the entry exists before trying to update it **/
        addCategoryEntry(CategoryHistory(
            accountKey = account,
            categoryKey = category,
            date = date,
            value = 0, // we'll update below
        ))
        updateCategoryHistoryTable(account, category, date, value)
    }

    @Transaction
    fun add(transaction: TransactionEntity): Long {
        Log.d("Libra/TransactionDao/add", "$transaction")
        val t = if (transaction.categoryKey == 0L) transaction.copy(
            categoryKey = if (transaction.value > 0) incomeKey else expenseKey
        ) else transaction

        val newKey = insert(t)
        if (t.accountKey <= 0) return newKey

        updateBalance(t.accountKey, t.value)
        updateCategoryHistory(t.accountKey, t.categoryKey, thisMonthEnd(t.date), t.valueAfterReimbursements)
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

        if (t.accountKey <= 0) return
        updateBalance(t.accountKey, -t.value)
        updateCategoryHistory(t.accountKey, t.categoryKey, thisMonthEnd(t.date), -t.valueAfterReimbursements)
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

        /** Need to also log difference to "Ignore category" for balance history total **/
        if (income.accountKey != expense.accountKey) {
            updateCategoryHistory(income.accountKey, ignoreKey, thisMonthEnd(income.date), value)
            updateCategoryHistory(expense.accountKey, ignoreKey, thisMonthEnd(expense.date), -value)
        }

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

        /** Need to also log difference to "Ignore category" for balance history total **/
        if (income.accountKey != expense.accountKey) {
            updateCategoryHistory(income.accountKey, ignoreKey, thisMonthEnd(income.date), -value)
            updateCategoryHistory(expense.accountKey, ignoreKey, thisMonthEnd(expense.date), value)
        }

        val new1 = if (t1.value > 0) newIncome else newExpense
        val new2 = if (t1.value > 0) newExpense else newIncome
        return Pair(new1, new2)
    }


    /** Value should always be positive **/
    @Transaction
    fun addAllocation(t: TransactionEntity, allocation: Allocation) {
        Log.d("Libra/TransactionDao/addAllocation", "t: $t")
        Log.d("Libra/TransactionDao/addAllocation", "a: $allocation")

        insert(allocation)
        if (t.accountKey <= 0) return

        val value = if (t.value > 0) allocation.value else -allocation.value
        val date = thisMonthEnd(t.date)
        updateCategoryHistory(
            account = t.accountKey,
            category = t.categoryKey,
            date = date,
            value = -value
        )
        updateCategoryHistory(
            account = t.accountKey,
            category = allocation.categoryKey,
            date = date,
            value = value
        )
    }

    /** Value should always be positive **/
    @Transaction
    fun removeAllocation(t: TransactionEntity, allocation: Allocation) {
        Log.d("Libra/TransactionDao/removeAllocation", "t: $t")
        Log.d("Libra/TransactionDao/removeAllocation", "a: $allocation")

        delete(allocation)
        if (t.accountKey <= 0) return

        val value = if (t.value > 0) allocation.value else -allocation.value
        val date = thisMonthEnd(t.date)
        updateCategoryHistory(
            account = t.accountKey,
            category = t.categoryKey,
            date = date,
            value = value
        )
        updateCategoryHistory(
            account = t.accountKey,
            category = allocation.categoryKey,
            date = date,
            value = -value
        )
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
            addAllocation(trans, it.copy(transactionKey = key))
        }
    }

    @Transaction
    fun undo(t: TransactionWithDetails) {
        var trans = t.transaction
        t.allocations.forEach {
            removeAllocation(trans, it)
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
