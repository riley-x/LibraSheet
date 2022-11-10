package com.example.librasheet.viewModel

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.database.*
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.toUi


class CategoryModel(
    private val viewModel: LibraViewModel,
) {
    private val data = CategoryData(viewModel.viewModelScope)

    /** This are displayed in both the categories settings screen and the respective cash flow
     * screens **/
    val income = mutableStateListOf<CategoryUi>()
    val expense = mutableStateListOf<CategoryUi>()

    /** Options to display when moving a category **/
    val moveTargets = mutableStateListOf<String>()

    /** Expanded state of each row in the edit category screen. This is needed here since lots of bugs
     * occur if you try to put it inside the LazyColumn::items. Index with the full category name.
     */
    val editScreenIsExpanded = mutableStateMapOf<String, MutableTransitionState<Boolean>>()


    suspend fun load() {
        // TODO, ensure listIndex is correct
    }

    fun loadUi() {
        income.clear()
        expense.clear()
        // TODO amounts
        val amounts = emptyMap<CategoryId, Float>()
        income.addAll(data.all[0].subCategories.map { it.toUi(amounts) })
        expense.addAll(data.all[1].subCategories.map { it.toUi(amounts) })
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

    fun checkError(fn: () -> String): String {
        val error = fn()
        if (error.isEmpty()) loadUi()
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
        loadUi()
    }
    @Callback
    fun reorder(parentId: String, startIndex: Int, endIndex: Int) {
        data.reorder(parentId, startIndex, endIndex)
        loadUi()
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

        return error
    }
}

