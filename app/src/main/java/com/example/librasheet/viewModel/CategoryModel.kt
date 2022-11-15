package com.example.librasheet.viewModel

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.entity.*
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.find
import com.example.librasheet.viewModel.dataClasses.toUi


class CategoryModel(
    private val viewModel: LibraViewModel,
) {
    val data = CategoryData(
        scope = viewModel.viewModelScope,
        dao = viewModel.application.database.categoryDao(),
        historyDao = viewModel.application.database.categoryHistoryDao()
    )

    /** This are used in the categories settings screen **/
    val income = mutableStateListOf<CategoryUi>()
    val expense = mutableStateListOf<CategoryUi>()

    /** These are used by the transaction and rule editors. These do NOT include "Income" or
     * "Expense". The last element is Category.Ignore. **/
    val incomeTargets = mutableStateListOf<Category>()
    val expenseTargets = mutableStateListOf<Category>()

    /** These are used by the transaction and rule editors. These DO include "Income" and
     * "Expense". The last element is Category.Ignore. **/
    val incomeFilters = mutableStateListOf<Category>()
    val expenseFilters = mutableStateListOf<Category>()

    /** These are displayed in the nested detail screens **/
    val incomeDetail = mutableStateListOf<CategoryUi>()
    val expenseDetail = mutableStateListOf<CategoryUi>()

    /** Options to display when moving a category **/
    val moveTargets = mutableStateListOf<String>()

    /** Expanded state of each row in the edit category screen. This is needed here since lots of bugs
     * occur if you try to put it inside the LazyColumn::items. Index with the full category name. **/
    val editScreenIsExpanded = mutableStateMapOf<String, MutableTransitionState<Boolean>>()

    fun loadData() = data.load()

    fun loadUi() {
        loadIncome()
        loadExpense()

        incomeTargets.clear()
        expenseTargets.clear()
        incomeTargets.addAll(data.all[0].getAllFlattened(false))
        expenseTargets.addAll(data.all[1].getAllFlattened(false))
        incomeTargets.add(Category.Ignore)
        expenseTargets.add(Category.Ignore)

        incomeFilters.clear()
        expenseFilters.clear()
        incomeFilters.add(data.all[0])
        expenseFilters.add(data.all[1])
        incomeFilters.addAll(incomeTargets)
        expenseFilters.addAll(expenseTargets)
    }


    fun loadDetail(list: SnapshotStateList<CategoryUi>, category: CategoryUi) {
        list.clear()
        list.addAll(category.subCategories)
    }

    private fun loadIncome() {
        income.clear()
        val amounts = emptyMap<Long, Long>()
        income.addAll(data.all[0].subCategories.map { it.toUi(amounts) })
    }
    private fun loadExpense() {
        expense.clear()
        val amounts = emptyMap<Long, Long>()
        expense.addAll(data.all[1].subCategories.map { it.toUi(amounts) })
    }


    @Callback
    fun loadIncomeDetail(category: CategoryUi) {
        loadDetail(incomeDetail, category)
        // TODO transactions, etc.
    }
    @Callback
    fun loadExpenseDetail(category: CategoryUi) {
        loadDetail(expenseDetail, category)
        // TODO
    }


    @Callback
    fun find(category: CategoryId) =
        income.find(category) ?:
        expense.find(category) ?:
        throw RuntimeException("CategoryModel couldn't find $category")


    @Callback
    fun setMoveOptions(category: CategoryId) {
        moveTargets.clear()
        if (category.isSub) moveTargets.add(category.superName)
        when (category.superName) {
            incomeName -> income
            expenseName -> expense
            else -> throw RuntimeException("CategoryModel::move bad category $category")
        }.filter { !category.isOrIsIn(it.id) }.mapTo(moveTargets) { it.id.fullName }
    }

    fun checkError(fn: () -> String): String {
        val error = fn()
        if (error.isEmpty()) viewModel.updateDependencies(Dependency.CATEGORY)
        return error
    }

    @Callback
    fun add(parentCategory: CategoryId, newCategory: String) = checkError {
        data.add(parentCategory, newCategory)
    }
    @Callback
    fun move(categoryId: CategoryId, newParentId: CategoryId) = checkError {
        data.move(categoryId, newParentId)
    }
    @Callback
    fun delete(categoryId: CategoryId) {
        data.delete(categoryId)
        viewModel.updateDependencies(Dependency.CATEGORY)
    }
    @Callback
    fun reorder(parentId: String, startIndex: Int, endIndex: Int) {
        data.reorder(parentId, startIndex, endIndex)
        viewModel.updateDependencies(Dependency.CATEGORY)
    }

    @Callback
    fun rename(categoryId: CategoryId, newName: String): String {
        val error = data.rename(categoryId, newName)
        if (error.isNotEmpty()) return error

        /** Update expanded state map. Note that because we enforce that categories only have one
         * level of nesting, the target category's children can never have an expanded state, so we
         * don't have to check for them **/
        editScreenIsExpanded.remove(categoryId.fullName)?.let {
            editScreenIsExpanded[joinCategoryPath(categoryId.parent, newName).fullName] = it
        }
        viewModel.updateDependencies(Dependency.CATEGORY)

        return ""
    }
}

