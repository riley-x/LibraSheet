package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.librasheet.data.database.Category
import com.example.librasheet.data.database.CategoryId
import com.example.librasheet.data.database.CategoryRule
import com.example.librasheet.data.rangeBetween
import com.example.librasheet.viewModel.dataClasses.CategoryUi

class RuleModel(
    private val viewModel: LibraViewModel,
) {
    /** Used by the CategoryRulesScreen **/
    val displayList = mutableStateListOf<CategoryRule>()
    var targetCategories  = mutableStateListOf<Category>()

    @Callback
    fun update(index: Int, pattern: String, category: Category) {
        Log.d("Libra/RuleMode/updateRule", "Enter: $index $pattern $category")
        val it = displayList[index]
        if (pattern == it.pattern && category == it.category) return
        displayList[index] = it.copy(
            pattern = pattern,
            categoryKey = category.key,
            category = category,
        )
        // TODO Room update. Only do a partial update since we don't keep track listIndex
        Log.d("Libra/RuleMode/updateRule", "Exit: ${displayList[index]}")
    }

    @Callback
    fun add(pattern: String, category: Category) {
        val rule = CategoryRule(
            pattern = pattern,
            categoryKey = category.key,
            category = category,
        )
        displayList.add(rule)
        // TODO Room update. Need wrapper insert function that finds the current last Index.
    }

    @Callback
    fun delete(index: Int) {
        displayList.removeAt(index)
        // TODO delete and update all affected indices (via a SQL command)
    }

    @Callback
    fun reorder(startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return
        displayList.add(endIndex, displayList.removeAt(startIndex))
        // TODO delete and update all affected indices (via a SQL command)
    }

    @Callback
    fun setFilter(income: Boolean) {
        val category = viewModel.categories.data.all[if (income) 0 else 1]
        targetCategories.clear()
        targetCategories.addAll(category.getAllFlattened(false))
        // TODO reset displayList via room query
    }

    @Callback
    fun setFilter(category: Category) {
        targetCategories.clear()
        targetCategories.addAll(category.getAllFlattened())
        // TODO reset displayList via room query
    }
}