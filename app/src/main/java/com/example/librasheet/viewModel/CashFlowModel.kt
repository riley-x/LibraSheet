package com.example.librasheet.viewModel

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.StackedLineGraphState
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.toUi

/**
 * This class contains the UI state for one cash flow screen.
 */
class CashFlowModel (
    private val data: CategoryData,
    private val isIncome: Boolean,
) {
    private var parentCategory: Category = Category.None
    private val multiplier = if (isIncome) 1f else -1f

    /** Pie chart **/
    val pie = mutableStateListOf<CategoryUi>()
    val pieRange = mutableStateOf(CategoryTimeRange.ONE_MONTH)

    /** History graph **/
    val history = StackedLineGraphState()
    val dates = mutableStateListOf<String>()
    val historyRange = mutableStateOf(HistoryTimeRange.ONE_YEAR)

    /** Expanded state of each row. This is needed here since lots of bugs occur if you try to put
     * it inside the LazyColumn::items. Index with the full category name. **/
    val isExpanded = mutableStateMapOf<String, MutableTransitionState<Boolean>>()

    fun load(category: Category) {
        parentCategory = category
        loadPie()
    }

    fun loadHistory() {

    }

    fun loadPie() {
        pie.clear()
        val amounts = when(pieRange.value) {
            CategoryTimeRange.ONE_MONTH -> data.currentMonth
            CategoryTimeRange.ONE_YEAR -> data.yearAverage
            CategoryTimeRange.ALL -> data.allAverage
        }
        pie.addAll(parentCategory.subCategories.map { it.toUi(amounts, multiplier) })

        val parentValue = multiplier * amounts.getOrDefault(parentCategory.key, 0L).toFloatDollar()
        if (parentValue > 0f) {
            pie.add(
                CategoryUi(
                    key = 0,
                    id = CategoryId("Uncategorized"),
                    color = parentCategory.color,
                    value = parentValue,
                )
            )
        }
    }

    @Callback
    fun setPieRange(range: CategoryTimeRange) {
        if (pieRange.value == range) return
        pieRange.value = range
        loadPie()
    }

    @Callback
    fun setHistoryRange(range: HistoryTimeRange) {
        if (historyRange.value == range) return
        historyRange.value = range
        loadHistory()
    }
}