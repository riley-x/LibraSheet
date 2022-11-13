package com.example.librasheet.data

import androidx.compose.runtime.Immutable
import com.example.librasheet.data.entity.AccountHistory


/**
 * Not used anymore...
 */

@Immutable
data class BalanceHistory(
    val date: Int,
    val balances: MutableMap<Long, Long> = mutableMapOf(),
) {
    val total: Long
        get() {
            var total = 0L
            balances.forEach { total += it.value }
            return total
        }
}


/** This takes a list of account history, assumed in increasing date order, and folds it into a list
 * indexed by (~date~, accountKey).
 */
fun List<AccountHistory>.foldAccounts(): MutableList<BalanceHistory> {
    val out = mutableListOf<BalanceHistory>()
    if (isEmpty()) return out

    var current = BalanceHistory(date = this[0].date)
    forEach {
        if (it.date != current.date) {
            out.add(current)
            current = BalanceHistory(date = it.date)
        }
        current.balances[it.accountKey] = it.balance
    }
    out.add(current)

    return out
}
