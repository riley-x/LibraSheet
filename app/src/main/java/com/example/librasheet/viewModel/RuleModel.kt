package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.CategoryRule
import com.example.librasheet.data.entity.matchCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/** Used by the CategoryRulesScreen.
 *
 * This class only stores a filtered set of rules at any given time. When we reorder rules,
 * we don't know what intermediate rules there may be. These have to be handled by the dao. Note
 * we don't need a startup function because all the loading is deferred to [setScreen].
 *
 *
 */
class RuleModel(
    private val viewModel: LibraViewModel,
) {
    private val dao = viewModel.application.database.ruleDao()

    val displayList = mutableStateListOf<CategoryRule>()
    var currentFilter by mutableStateOf(Category.None)
    var currentScreenIsIncome by mutableStateOf(false)


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
        val ruleWithoutKey = CategoryRule(
            pattern = pattern,
            categoryKey = category.key,
            category = category,
            isIncome = currentScreenIsIncome,
        )
        viewModel.viewModelScope.launch {
            // TODO loading indicator
            displayList.add(ruleWithoutKey.copy(
                key = withContext(Dispatchers.IO) { dao.add(ruleWithoutKey) }
            ))
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
        val category = if (income) viewModel.categories.data.income else viewModel.categories.data.expense
        setFilter(category)
    }

    @Callback
    fun setFilter(category: Category) {
        currentFilter = category
        viewModel.viewModelScope.launch {
            // TODO loading indicator
            val rules = withContext(Dispatchers.IO) {
                val rules = if (currentScreenIsIncome) {
                    if (category.key == Category.Ignore.key) dao.getIgnoredIncome()
                    else if (category.id.isSuper) dao.getIncomeRules()
                    else dao.getIncomeRules(category.key)
                }
                else {
                    if (category.key == Category.Ignore.key) dao.getIgnoredExpense()
                    else if (category.id.isSuper) dao.getExpenseRules()
                    else dao.getExpenseRules(category.key)
                }
                // TODO is reading category safe here?
                rules.matchCategories(category)
            }
            displayList.clear()
            displayList.addAll(rules)
        }
    }
}