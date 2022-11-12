package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.database.Category
import com.example.librasheet.data.database.CategoryId
import com.example.librasheet.data.database.CategoryRule
import com.example.librasheet.data.rangeBetween
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RuleModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.ruleDao()

    /** Used by the CategoryRulesScreen **/
    val displayList = mutableStateListOf<CategoryRule>()
    var targetCategories = mutableStateListOf<Category>()
    var filterCategories = mutableStateListOf<Category>()
    var currentFilter by mutableStateOf(Category.None)
    private var currentScreenIsIncome = false


    /** This class only stores a filtered set of rules at any given time. When we reorder rules,
     * we don't know what intermediate rules there may be. These have to be handled by the dao.
     */

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
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.update(displayList[index])
        }
        Log.d("Libra/RuleMode/updateRule", "Exit: ${displayList[index]}")
    }

    @Callback
    fun add(pattern: String, category: Category) {
        val rule = CategoryRule(
            pattern = pattern,
            categoryKey = category.key,
            category = category,
            isIncome = currentScreenIsIncome,
        )
        displayList.add(rule)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.add(rule)
        }
    }

    @Callback
    fun delete(index: Int) {
        val rule = displayList.removeAt(index)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.delete(rule.key)
            /** We don't need to update indices here, since we don't maintain that the indices have
             * to be compact, just in order.
             */
        }
    }

    @Callback
    fun reorder(startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return
        val displacedRule = displayList[endIndex]
        val movedRule = displayList.removeAt(startIndex)
        displayList.add(endIndex, movedRule)

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            dao.move(movedRule, displacedRule)
        }
    }

    /** Called from navigating to the income or expense rules from the settings screen. **/
    @Callback
    fun setScreen(income: Boolean) {
        currentScreenIsIncome = income
        val category = viewModel.categories.data.all[if (income) 0 else 1]
        targetCategories.clear()
        targetCategories.addAll(category.getAllFlattened(false))
        filterCategories.clear()
        filterCategories.add(category)
        filterCategories.addAll(targetCategories)
        setFilter(category)
    }

    @Callback
    fun setFilter(category: Category) {
        currentFilter = category
        // TODO reset displayList via room query
    }
}