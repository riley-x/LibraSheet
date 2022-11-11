package com.example.librasheet.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.librasheet.data.database.Category
import com.example.librasheet.data.database.CategoryId
import com.example.librasheet.data.database.CategoryRule
import com.example.librasheet.viewModel.dataClasses.CategoryUi

class RuleModel(
    private val viewModel: LibraViewModel,
) {
    private var lastIndex = 0 // TODO

    /** Used by the CategoryRulesScreen **/
    val displayList = mutableStateListOf<CategoryRule>()
    var filterCategory by mutableStateOf(Category.None)

    @Callback
    fun updateRule(index: Int, pattern: String, category: Category) {
        val it = displayList[index]
        if (pattern == it.pattern || category.key == it.categoryKey) return
        displayList[index] = it.copy(
            pattern = pattern,
            categoryKey = category.key,
            category = category,
        )
        // TODO Room update
    }

    @Callback
    fun addRule(pattern: String, category: Category) {
        lastIndex += 1
        val rule = CategoryRule(
            pattern = pattern,
            categoryKey = category.key,
            listIndex = lastIndex,
            category = category,
        )
        displayList.add(rule)
        // TODO Room update
    }

    @Callback
    fun setFilter(income: Boolean) {
        filterCategory = viewModel.categories.data.all[if (income) 0 else 1]
    }
}