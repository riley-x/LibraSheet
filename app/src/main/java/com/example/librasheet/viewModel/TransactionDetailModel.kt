package com.example.librasheet.viewModel

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.librasheet.data.dao.TransactionWithDetails
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.toDoubleDollar
import com.example.librasheet.data.toIntDate
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.formatDateIntSimple
import com.example.librasheet.ui.components.parseOrNull
import java.text.SimpleDateFormat
import kotlin.math.abs
import kotlin.math.absoluteValue

class TransactionDetailModel(
    val onSave: (new: TransactionWithDetails, old: TransactionWithDetails) -> Boolean = { _,_ -> true },
) {
    val account = mutableStateOf<Account?>(null)
    val category = mutableStateOf<Category?>(null)
    val name = mutableStateOf("")
    val date = mutableStateOf("")
    val value = mutableStateOf("")
    val reimbursements = mutableStateListOf<ReimbursementWithValue>()
    val allocations = mutableStateListOf<Allocation>()

    val dateError = mutableStateOf(false)
    val valueError = mutableStateOf(false)

    /** Old edit/details **/
    private var oldDetail = TransactionWithDetails(
        transaction = TransactionEntity(),
        reimbursements = emptyList(),
        allocations = emptyList(),
    )
    private val oldKey
        get() = oldDetail.transaction.key


    @Callback
    fun load(t: TransactionWithDetails, account: Account?) {
        oldDetail = t
        dateError.value = false
        valueError.value = false

        this.account.value = account
        category.value = t.transaction.category
        name.value = t.transaction.name
        date.value = formatDateIntSimple(t.transaction.date, "-")
        value.value = if (t.transaction.value == 0L) "" else t.transaction.value.toDoubleDollar().toString()

        reimbursements.clear()
        reimbursements.addAll(t.reimbursements)

        allocations.clear()
        allocations.addAll(t.allocations)
    }

    fun isIncome() = (value.value.toDoubleOrNull() ?: 0.0) > 0

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("MM-dd-yy").apply { isLenient = false }

    private fun createTransaction(): TransactionEntity? {
        val dateInt = formatter.parseOrNull(date.value)?.toIntDate()
        val valueLong = value.value.toDoubleOrNull()?.toLongDollar()

        dateError.value = dateInt == null
        valueError.value = valueLong == null
        if (dateError.value || valueError.value) return null

        return TransactionEntity(
            key = oldKey,
            name = name.value,
            date = dateInt ?: 0,
            value = valueLong ?: 0L,
            category = category.value ?: Category.None,
            categoryKey = category.value?.key ?: 0,
            accountKey = account.value?.key ?: 0,
        )
    }

    @Callback
    fun save(): Boolean {
        val new = TransactionWithDetails(
            transaction = createTransaction() ?: return false,
            reimbursements = reimbursements,
            allocations = allocations,
        )
        return onSave(new, oldDetail)
    }



    /**
     * Note these only apply to the ui state. The actual database action happens on save.
     */
    @Callback
    fun addReimbursement(t: TransactionEntity) {
        val targetValue = abs(t.valueAfterReimbursements)
        val currentValue = value.value.toDoubleOrNull()?.toLongDollar()?.absoluteValue
        reimbursements.add(
            ReimbursementWithValue(
                transaction = t,
                value = if (currentValue != null) minOf(targetValue, currentValue) else targetValue
            )
        )
    }

    @Callback
    fun deleteReimbursement(index: Int) {
        reimbursements.removeAt(index)
    }

    @Callback
    fun changeReimbursementValue(index: Int, value: Long) {
        if (index !in reimbursements.indices) return
        reimbursements[index] = reimbursements[index].copy(
            value = value
        )
    }

    @Callback
    fun addAllocation(name: String, value: Long, category: Category?) {
        allocations.add(
            Allocation(
                key = 0,
                name = name,
                transactionKey = oldKey,
                categoryKey = category?.key ?: 0,
                value = value,
                listIndex = allocations.size, // TODO edit listIndexes on save
            ).also { it.category = category ?: Category.None }
        )
    }
    @Callback
    fun editAllocation(i: Int, name: String, value: Long, category: Category?) {
        allocations[i] = Allocation(
            key = 0,
            name = name,
            transactionKey = oldKey,
            categoryKey = category?.key ?: 0,
            value = value,
            listIndex = i, // TODO edit listIndexes on save
        ).also { it.category = category ?: Category.None }
    }
    @Callback
    fun reorderAllocation(start: Int, end: Int) {
        if (start !in allocations.indices || end !in allocations.indices) return
        allocations.add(end, allocations.removeAt(start))
        // TODO database
    }
    @Callback
    fun deleteAllocation(index: Int) {
        allocations.removeAt(index)
    }
}