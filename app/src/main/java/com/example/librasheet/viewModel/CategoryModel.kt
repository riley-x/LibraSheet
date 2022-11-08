package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.database.CategoryEntity
import com.example.librasheet.data.database.CategoryWithChildren
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.dataClasses.toCategory
import com.example.librasheet.viewModel.preview.previewExpenseCategories
import com.example.librasheet.viewModel.preview.previewIncomeCategories



class CategoryModel(private val parent: LibraViewModel) {
    internal val data = CategoryData(parent.viewModelScope)

    /** This are displayed in the categories settings screen, but are also used to calculate the
     * cash flow screen categories.
     */
    val income = mutableStateListOf<Category>()
    val expense = mutableStateListOf<Category>()

    suspend fun loadData() {
        // TODO
    }

    fun loadUi() {
        fun loadList(list: MutableList<Category>, data: List<CategoryWithChildren>) {
            list.clear()
            data.mapTo(list) {
                it.parent.toCategory().copy(
                    subCategories = it.children.map(CategoryEntity::toCategory)
                )
            }
        }
        loadList(income, data.incomeEntities)
        loadList(expense, data.expenseEntities)
    }

    @Callback
    fun add(parentCategory: String, newCategory: String): Boolean {
        if (data.add(parentCategory, newCategory)) {
            loadUi()
            return true
        }
        return false
    }

    @Callback
    fun rename(currentCategory: String, newName: String) {

    }
}

