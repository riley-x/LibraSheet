package com.example.librasheet.data.dao

import androidx.room.*
import com.example.librasheet.data.HistoryEntryBase
import com.example.librasheet.data.TimeSeries
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.monthDiff
import com.example.librasheet.data.toDoubleDollar


@Dao
interface CategoryHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(it: CategoryHistory)

//    @Query("SELECT 0 as seriesKey, date, SUM(value) as value FROM $categoryHistoryTable " +
//            "WHERE value > 0 AND accountKey = :account GROUP BY date ORDER BY date")
//    fun getIncome(account: Long): List<TimeSeries>
//
//    @Query("SELECT 1 as seriesKey, date, SUM(value) as value FROM $categoryHistoryTable " +
//            "WHERE value < 0 AND accountKey = :account GROUP BY date ORDER BY date")
//    fun getExpense(account: Long): List<TimeSeries>
//
//    @Query("SELECT date, SUM(value) as value FROM $categoryHistoryTable " +
//            "WHERE accountKey = :account GROUP BY date ORDER BY date")
//    fun getNetIncome(account: Long): List<TimeSeries>

    /**
     * Returns total income and expenses for a single account, per date (month). Income is set to
     * seriesKey = 0 and expense seriesKey = 1. Note that the ignoreCategory is excluded here.
     */
    @Query("SELECT CASE WHEN value > 0 THEN 0 ELSE 1 END as seriesKey, date, SUM(value) as value FROM $categoryHistoryTable " +
            "WHERE accountKey = :account AND categoryKey != $ignoreKey AND value != 0 GROUP BY seriesKey, date " +
            "ORDER BY date")
    fun getIncomeAndExpense(account: Long): List<HistoryEntryBase>


    @Query("SELECT date, SUM(value) as value FROM $categoryHistoryTable " +
            "WHERE categoryKey != $ignoreKey AND value != 0 GROUP BY date ORDER BY date")
    fun getNetIncome(): List<TimeSeries>

    /** Don't use these average functions. If a category is 0 for a month, it'll not have an entry,
     * and the average will be inaccurate.
     */
//    @MapInfo(keyColumn = "categoryKey", valueColumn = "average")
//    @Query("SELECT categoryKey, AVG(sums) as average FROM (" +
//            "SELECT categoryKey, date, SUM(value) as sums " +
//            "FROM $categoryHistoryTable WHERE date >= :startDate AND date <= :endDate GROUP BY categoryKey, date" +
//            ") GROUP BY categoryKey")
//    fun getAverages(startDate: Int, endDate: Int): Map<Long, Long>

    @MapInfo(keyColumn = "categoryKey", valueColumn = "sums")
    @Query("SELECT categoryKey, SUM(value) as sums " +
            "FROM $categoryHistoryTable WHERE date = :date AND value != 0 " +
            "GROUP BY categoryKey")
    fun getDate(date: Int): Map<Long, Long>

    @Query("SELECT accountKey, categoryKey, date, SUM(value) as value FROM $categoryHistoryTable " +
            "WHERE value != 0 " +
            "GROUP BY date, categoryKey ORDER BY date")
    fun getAll(): List<CategoryHistory>


    @MapInfo(keyColumn = "categoryKey", valueColumn = "sums")
    @Query("SELECT categoryKey, SUM(value) as sums " +
            "FROM $categoryHistoryTable WHERE date >= :startDate AND value != 0 " +
            "GROUP BY categoryKey")
    fun getTotals(startDate: Int): Map<Long, Long>

    @MapInfo(keyColumn = "categoryKey", valueColumn = "sums")
    @Query("SELECT categoryKey, SUM(value) as sums " +
            "FROM $categoryHistoryTable WHERE date >= :startDate AND date <= :endDate AND value != 0 " +
            "GROUP BY categoryKey")
    fun getTotals(startDate: Int, endDate: Int): Map<Long, Long>

    fun getAverages(startDate: Int, endDate: Int): Map<Long, Double> =
        getTotals(startDate, endDate).mapValues { it.value.toDoubleDollar() / (monthDiff(endDate, startDate) + 1) }

    @Query("SELECT MIN(date) FROM $categoryHistoryTable WHERE value != 0")
    fun getEarliestDate(): Int
}