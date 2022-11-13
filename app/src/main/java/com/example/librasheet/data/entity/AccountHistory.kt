package com.example.librasheet.data.entity

import androidx.room.Entity

const val accountHistoryTable = "account_history"

@Entity(
    tableName = accountHistoryTable,
    primaryKeys = ["accountKey", "date"],
)
data class AccountHistory(
    val accountKey: Long,
    val date: Int,
    val balance: Long,
)


/** This takes a list of account history, assumed in increasing date order, and folds it into a
 * map accountKey -> balances. The balances of each account will be zero padded so they all have the
 * same length. **/
fun List<AccountHistory>.alignDates(): Pair<MutableList<Int>, MutableMap<Long, MutableList<Long>>> {
    /** Outputs **/
    val dates = mutableListOf<Int>()
    val balances = mutableMapOf<Long, MutableList<Long>>()
    if (isEmpty()) return Pair(dates, balances)

    /** Aggregators. We assume the list is in date order, so same dates should be next to each other.
     * Collect values until we find a new date, at which point we update the former date. **/
    var currentDate = this[0].date
    val currentValues = mutableMapOf<Long, Long>()
    fun update(newDate: Int) {
        balances.forEach { (account, list) ->
            list.add(currentValues.getOrElse(account) { list.lastOrNull() ?: 0 })
        }
        currentValues.clear()
        dates.add(currentDate) // this should happen at end of block not beginning, so that newly added accounts are correctly in-sync.
        currentDate = newDate
    }

    /** Main loop **/
    forEach {
        if (it.date != currentDate) update(it.date)
        if (it.accountKey !in balances) {
            balances[it.accountKey] = List(dates.size) { 0L }.toMutableList()
        }
        currentValues[it.accountKey] = it.balance
    }
    update(0)

    return Pair(dates, balances)
}