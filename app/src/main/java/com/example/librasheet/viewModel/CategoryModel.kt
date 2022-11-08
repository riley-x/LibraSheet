package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.database.CategoryEntity
import com.example.librasheet.viewModel.dataClasses.*


class CategoryModel(private val parent: LibraViewModel) {
    internal val data = CategoryData(parent.viewModelScope)

    /** This are displayed in the categories settings screen, but are also used to calculate the
     * cash flow screen categories.
     */
    val income = mutableStateListOf<Category>()
    val expense = mutableStateListOf<Category>()

    val moveTargets = mutableStateListOf<String>()

    suspend fun loadData() {
        // TODO
    }

    fun loadUi() {
        fun loadList(list: MutableList<Category>, data: List<CategoryEntity>) {
            list.clear()
            data.mapTo(list) { it.toCategory() }
        }
        loadList(income, data.incomeEntities)
        loadList(expense, data.expenseEntities)
    }

    @Callback
    fun add(parentCategory: String, newCategory: String): Boolean {
        if (newCategory.contains(categoryPathSeparator)) return false // TODO change error message
        if (data.add(parentCategory, newCategory)) {
            loadUi()
            return true
        }
        return false
    }

    @Callback
    fun rename(currentCategory: String, newName: String): Boolean {
        if (newName.contains(categoryPathSeparator)) return false // TODO change error message
        if (data.rename(currentCategory, newName)) {
            loadUi()
            return true
        }
        return false
    }

    @Callback
    fun setMoveOptions(currentCategory: String) {
        val superName = getSuperCategory(currentCategory)
        moveTargets.clear()
        moveTargets.add(superName)
        when (superName) {
            incomeName -> income
            expenseName -> expense
            else -> throw RuntimeException("CategoryModel::move bad category $currentCategory")
        }.filter { it.id != currentCategory }.mapTo(moveTargets) { it.name }
    }

    @Callback
    fun move(currentCategory: String, newParent: String): Boolean {
        return false
    }
}

