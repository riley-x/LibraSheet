package com.example.librasheet.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.librasheet.data.database.CategoryId
import com.example.librasheet.data.database.CategoryRule
import com.example.librasheet.viewModel.dataClasses.CategoryUi

class RuleModel(
    private val viewModel: LibraViewModel,
) {
    /** Used by the CategoryRulesScreen **/
    val displayList = mutableStateListOf<CategoryRule>()
    var filterCategory = mutableStateOf(CategoryId())

    @Callback
    fun updateRule(index: Int, pattern: String, category: CategoryUi) {
        val it = displayList[index]
        if (pattern == it.pattern || category.key == it.categoryKey) return
        displayList[index] = it.copy(
            pattern = pattern,
            categoryKey = category.key,
            category = category,
        )
        // TODO Room update
    }
}