package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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

    /** Options to display when moving a category **/
    val moveTargets = mutableStateListOf<String>()

    /** Expanded state of each row in the edit category screen. This is needed here since lots of bugs
     * occur if you try to put it inside the LazyColumn::items. Index with the full category name.
     */
    val editScreenIsExpanded = mutableStateMapOf<String, Boolean>()

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
    fun add(parentCategory: CategoryId, newCategory: String): String {
        if (newCategory.contains(categoryPathSeparator)) return "Error: name can't contain underscores"
        if (data.add(parentCategory, newCategory)) {
            loadUi()
            return ""
        }
        return "Error: account exists already"
    }

    @Callback
    fun rename(currentCategory: CategoryId, newName: String): String {
        if (newName.contains(categoryPathSeparator)) return "Error: name can't contain underscores"
        if (data.rename(currentCategory, newName)) {
            loadUi()
            return ""
        }
        return "Error: account exists already"
    }

    @Callback
    fun setMoveOptions(currentCategory: CategoryId) {
        moveTargets.clear()
        if (currentCategory.isSub) moveTargets.add(currentCategory.superName)
        when (currentCategory.superName) {
            incomeName -> income
            expenseName -> expense
            else -> throw RuntimeException("CategoryModel::move bad category $currentCategory")
        }.filter { it.id.topName != currentCategory.topName }.mapTo(moveTargets) { it.id.fullName }
    }

    @Callback
    fun move(currentCategory: CategoryId, newParent: CategoryId): String {
        val error = data.move(currentCategory, newParent)
        if (error.isNotBlank()) return error
        loadUi()
        return ""
    }

    @Callback
    fun delete(categoryId: CategoryId) {
        // TODO delete transaction crossrefs
        data.delete(categoryId)
        loadUi()
    }

    @Callback
    fun reorder(parentId: String, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return
        data.reorder(parentId.toCategoryId(), startIndex, endIndex)
        loadUi()
    }
}

