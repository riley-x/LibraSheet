package com.example.librasheet.data

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.dao.CategoryDao
import com.example.librasheet.data.dao.CategoryHistoryDao
import com.example.librasheet.data.entity.*
import com.example.librasheet.ui.theme.randomColor
import kotlinx.coroutines.*
import java.util.*

class CategoryData(
    private val scope: CoroutineScope,
    private val dao: CategoryDao,
    val historyDao: CategoryHistoryDao,
) {
    private var lastRowId = 0L

    val all = Category(
        key = allKey,
        id = CategoryId(),
        color = Color.Unspecified,
        subCategories = mutableListOf(
            Category(
                key = incomeKey,
                id = CategoryId(incomeName),
                color = Color(0xFF004940),
                listIndex = 0,
            ),
            Category(
                key = expenseKey,
                id = CategoryId(expenseName),
                color = Color(0xFF5C1604),
                listIndex = 1,
            ),
            Category.Ignore,
        )
    )
    val income: Category
        get() = all.subCategories[0]
    val expense: Category
        get() = all.subCategories[1]

    var historyDates = mutableListOf<Int>()
    var history = mutableMapOf<Long, MutableList<Long>>()

    /** Map categoryKey to value. This is inclusive of all accounts. These are used for the pie
     * charts and display lists. **/
    val today = Calendar.getInstance().toIntDate()
    val thisMonthEnd = thisMonthEnd(today)
    var firstMonthEnd = 0 // Set in loadValues
    val lastMonthEnd = today.setDay(0)
    val lastYearEnd = lastMonthEnd.addYears(-1)

    var maxMonths = 1 // Max number of months stored in database. Excludes the current month.
                      // Set in loadValues and used in averages.
    var currentMonth = emptyMap<Long, Double>()
    var yearAverage = emptyMap<Long, Double>()
    var allAverage = emptyMap<Long, Double>()

    var yearTotal = emptyMap<Long, Double>()
    var fiveYearTotal = emptyMap<Long, Double>()
    var allTotal = emptyMap<Long, Double>()


    private fun MutableList<Job>.launchIO(fn: suspend CoroutineScope.() -> Unit) =
        add(scope.launch(Dispatchers.IO, block = fn))


    fun loadCategories(): List<Job> {
        val jobs = mutableListOf<Job>()

        /** Index **/
        jobs.launchIO {
            lastRowId = dao.getMaxKey()
            Log.d("Libra/CategoryData/load", "lastRowId=$lastRowId")
        }

        /** Categories **/
        jobs.launchIO {
            income.subCategories.clear()
            dao.getIncome().mapTo(income.subCategories) { it.toNestedCategory() }
            Log.d("Libra/CategoryData/load", "income=${income.subCategories.size}")
        }
        jobs.launchIO {
            expense.subCategories.clear()
            dao.getExpense().mapTo(expense.subCategories) { it.toNestedCategory() }
            Log.d("Libra/CategoryData/load", "expense=${expense.subCategories.size}")
        }

        return jobs
    }

    /** Category history. This relies on the categories to be loaded first. **/
    fun loadHistory(months: List<Int>): List<Job> {
        val jobs = mutableListOf<Job>()

        jobs.launchIO {
            historyDates = months.toMutableList()
            history = historyDao.getAll().alignDates(dates = months, cumulativeSum = false)
            /** Sum subCategories into parents. **/
            income.sumChildren(history)
            expense.sumChildren(history)
            Log.d("Libra/CategoryData/load", "history=$history")
        }

        return jobs
    }

    fun loadValues(): List<Job> {
        val jobs = mutableListOf<Job>()

        /** Averages **/
        jobs.launchIO {
            currentMonth = historyDao.getDate(thisMonthEnd).mapValues { it.value.toDoubleDollar() }
            Log.d("Libra/CategoryData/load", "currentMonth=$currentMonth")
        }
        jobs.launchIO {
            firstMonthEnd = historyDao.getEarliestDate()
            maxMonths = maxOf(1, monthDiff(thisMonthEnd, firstMonthEnd))
            Log.d("Libra/CategoryData/load", "maxMonths=$maxMonths thisMonth=$thisMonthEnd firstMonthEnd=$firstMonthEnd")

            yearAverage = historyDao.getTotals(lastYearEnd, lastMonthEnd).mapValues { it.value.toDoubleDollar() / minOf(maxMonths, 12) }
            allAverage = historyDao.getTotals(0, lastMonthEnd).mapValues { it.value.toDoubleDollar() / maxMonths }
        }

        /** Totals **/
        jobs.launchIO {
            yearTotal = historyDao.getTotals(lastYearEnd).mapValues { it.value.toDoubleDollar() }
            Log.d("Libra/CategoryData/load", "yearTotal=$yearTotal")
        }
        jobs.launchIO {
            fiveYearTotal = historyDao.getTotals(lastMonthEnd.addYears(-5)).mapValues { it.value.toDoubleDollar() }
            Log.d("Libra/CategoryData/load", "fiveYearTotal=$fiveYearTotal")
        }
        jobs.launchIO {
            allTotal = historyDao.getTotals(0).mapValues { it.value.toDoubleDollar() }
            Log.d("Libra/CategoryData/load", "allTotal=$allTotal")
        }

        return jobs
    }

    fun find(category: CategoryId): Triple<Category, MutableList<Category>, Int> =
        all.subCategories.find(category) ?: throw RuntimeException("CategoryModel::find couldn't find $category")


    fun add(parentCategory: CategoryId, newCategory: String): String {
        /** Find parent with error checks **/
        if (newCategory.contains(categoryPathSeparator)) return "Error: name can't contain underscores"
        val (parent, _) = find(parentCategory)
        if (parent.subCategories.any { it.id.name == newCategory }) return "Error: category exists already"

        /** Create and add the category **/
        lastRowId += 1
        val category = Category(
            key = lastRowId,
            id = joinCategoryPath(parentCategory, newCategory),
            color = randomColor(),
            parentKey = parent.key,
            listIndex = parent.subCategories.size,
        )
        parent.subCategories.add(category)

        /** Update the database **/
        scope.launch(Dispatchers.IO) {
            Log.d("Libra/CategoryData/add", "$category")
            dao.add(category)
        }
        return ""
    }


    fun rename(categoryId: CategoryId, newName: String): String {
        /** Find category with error checks **/
        if (newName.contains(categoryPathSeparator)) return "Error: name can't contain underscores"
        val (category, parentList, index) = find(categoryId)
        if (parentList.any { it.id.name == newName }) return "Error: account exists already"

        /** Update target **/
        category.id = joinCategoryPath(category.id.parent, newName)

        /** Update target's children. Note because we enforce that categories only have one level of
         * nesting, at most we have to check the target's children, not nested ones. Similarly, it's
         * children can never have an expanded state, so we don't have to update the map. **/
        val staleList = mutableListOf(category)
        for (child in category.subCategories) {
            child.id = joinCategoryPath(category.id, child.id.name)
            staleList.add(child)
        }

        /** Update database **/
        scope.launch(Dispatchers.IO) {
            dao.update(staleList)
        }
        return ""
    }


    fun move(categoryId: CategoryId, newParentId: CategoryId): String {
        val (category, oldParentList, index) = find(categoryId)
        if (category.subCategories.isNotEmpty())
            return "Error: can't move category with subcategories"

        val (newParent, _) = find(newParentId)
        if (newParent.subCategories.any { it.id.name == categoryId.name })
            return "Error: category exists in destination already"

        /** Update add to new parent **/
        category.apply {
            id = joinCategoryPath(newParentId, categoryId.name)
            parentKey = newParent.key
            listIndex = newParent.subCategories.size
        }
        newParent.subCategories.add(category)

        /** Update old parent's list and its children's indices. We should do this here because we
         * need to know which categories (PKs) to update, and that's hard to get from SQL. This loop
         * can't be in a dispatched thread though since it reads a list that could be modified
         * concurrently (would have to copy parentList). **/
        val staleList = mutableListOf(category)
        oldParentList.removeAt(index)
        for (i in index..oldParentList.lastIndex) {
            oldParentList[i].listIndex = i
            staleList.add(oldParentList[i])
        }

        /** Update database **/
        scope.launch(Dispatchers.IO) {
            dao.update(staleList)
        }
        return ""
    }


    fun delete(categoryId: CategoryId) {
        val (category, parentList, index) = find(categoryId)
        parentList.removeAt(index)

        /** Other objects may store a Category pointer. The removal from the list here will not
         * invalidate those pointers, and the object won't be garbage collected either. So we need
         * to manually set it to None. **/
        val deletedCategories = category.reset()

        /** Update old parent's list and its children's indices. We should do this here because we
         * need to know which categories (PKs) to update, and that's hard to get from SQL. This loop
         * can't be in a dispatched thread though since it reads a list that could be modified
         * concurrently (would have to copy parentList). **/
        val staleList = mutableListOf<Category>()
        for (i in index..parentList.lastIndex) {
            parentList[i].listIndex = i
            staleList.add(parentList[i])
        }

        /** Update database **/
        scope.launch(Dispatchers.IO) {
            dao.deleteUpdate(deletedCategories, staleList)
        }
    }


    fun reorder(parentId: String, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return

        val (parent, _) = find(parentId.toCategoryId())
        if (startIndex !in parent.subCategories.indices) return // These happen if you try to reorder the "Uncategorized" entry
        if (endIndex !in parent.subCategories.indices) return

        parent.subCategories.add(endIndex, parent.subCategories.removeAt(startIndex))

        /** Update old parent's list and its children's indices. We should do this here because we
         * need to know which categories (PKs) to update, and that's hard to get from SQL. This loop
         * can't be in a dispatched thread though since it reads a list that could be modified
         * concurrently (would have to copy parentList). **/
        val staleEntities = mutableListOf<Category>()
        for (i in rangeBetween(startIndex, endIndex)) {
            parent.subCategories[i].listIndex = i
            staleEntities.add(parent.subCategories[i])
        }

        /** Update database **/
        scope.launch(Dispatchers.IO) {
            dao.update(staleEntities)
        }
    }
}

/** The values retrieved from the database are per category, not pre-summed by parent/child hierarchy.
 * This function edits a returned history map of category -> history (retrieved from alignDates) to
 * add component categories to their parents. The return value is the history list of this category,
 * and is just used for recursion.
 */
fun Category.sumChildren(values: MutableMap<Long, MutableList<Long>>): List<Long>? {
    var list = values.getOrDefault(key, null)
    subCategories.forEach { sub ->
        sub.sumChildren(values)?.let {
            if (list == null) {
                list = it.toMutableList()
                values[key] = list!!
            }
            else {
                for (i in it.indices) {
                    list!![i] += it[i]
                }
            }
        }
    }
    return list
}