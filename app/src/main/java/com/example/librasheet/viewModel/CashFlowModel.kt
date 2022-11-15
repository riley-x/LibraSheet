package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.stackedLineGraphValues
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.components.formatOrder
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.toUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * This class contains the UI state for one cash flow screen.
 */
class CashFlowModel (
    private val scope: CoroutineScope,
    private val data: CategoryData,
    private val isIncome: Boolean,
) {
    var parentCategory: Category = Category.None
    private val multiplier = if (isIncome) 1f else -1f

    private val graphYPad = 0.1f
    private val graphTicksX = 4
    private val graphTicksY = 6

    /** Pie chart **/
    val pie = mutableStateListOf<CategoryUi>()
    val pieRange = mutableStateOf(CategoryTimeRange.ONE_MONTH)

    /** History graph **/
    private var fullHistory = listOf<StackedLineGraphValue>()
    private var fullDates = listOf<String>()

    val history = StackedLineGraphState()
    val dates = mutableStateListOf<String>()
    val historyRange = mutableStateOf(HistoryTimeRange.ONE_YEAR)

    /** Expanded state of each row. This is needed here since lots of bugs occur if you try to put
     * it inside the LazyColumn::items. Index with the full category name. **/
    val isExpanded = mutableStateMapOf<String, MutableTransitionState<Boolean>>()

    fun load(category: Category = parentCategory) {
        parentCategory = category

        if (parentCategory.key == 0L) {
            pie.clear()
            fullHistory = emptyList()
            fullDates = emptyList()
            history.values.clear()
            dates.clear()
        } else {
            loadPie()
            scope.launch {
                loadFullHistory()
                loadHistory()
            }
        }
    }

    fun loadPie() {
        val amounts = when(pieRange.value) {
            CategoryTimeRange.ONE_MONTH -> data.currentMonth
            CategoryTimeRange.ONE_YEAR -> data.yearAverage
            CategoryTimeRange.ALL -> data.allAverage
        }
        pie.clear()
        pie.addAll(parentCategory.subCategories.map { it.toUi(amounts, multiplier) })

        val parentValue = multiplier * amounts.getOrDefault(parentCategory.key, 0L).toFloatDollar()
        if (parentValue > 0f) {
            pie.add(
                CategoryUi(
                    category = Category.Ignore,
                    key = ignoreKey,
                    id = CategoryId("Uncategorized"),
                    color = parentCategory.color,
                    value = parentValue,
                )
            )
        }
    }


    suspend fun loadFullHistory() {
        fullDates = data.historyDates.map {
            formatDateInt(it, "MMM yyyy")
        }

        val categoryList = parentCategory.subCategories + listOf(Category(
            key = parentCategory.key,
            id = CategoryId("Uncategorized"),
            color = parentCategory.color,
        ))

        fullHistory = withContext(Dispatchers.Default) {
            data.history.stackedLineGraphValues(categoryList, multiplier).first
        }
        Log.d("Libra/CashFlowModel/loadFullHistory", "$fullDates")
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

    private fun <T> List<T>.takeLastOrAll(n: Int) = if (n >= 0) takeLast(n) else this

    private fun loadHistory() {
        if (fullHistory.isEmpty()) return

        val n = when (historyRange.value) {
            HistoryTimeRange.ONE_YEAR -> 12
            HistoryTimeRange.FIVE_YEARS -> 60
            HistoryTimeRange.ALL -> -1
        }

        dates.clear()
        dates.addAll(fullDates.takeLastOrAll(n))

        history.values.clear()
        fullHistory.mapTo(history.values) {
            it.copy(second = it.second.takeLastOrAll(n))
        }

        // We only need to look at [0] because that's the top stack
        val maxY = history.values[0].second.max()
        val ticksX = autoXTicksDiscrete(dates.size, graphTicksX) { dates[it] }
        val (ticksY, order) = autoYTicksWithOrder(0f, maxY, graphTicksY)
        val axes = AxesState(
            ticksY = ticksY,
            ticksX = ticksX,
            minY = 0f,
            maxY = maxY + maxY * graphYPad,
            minX = 0f,
            maxX = dates.lastIndex.toFloat(),
        )

        history.axes.value = axes
        history.toString.value = { formatOrder(it, order) }

        Log.d("Libra/CashFlowModel/loadHistory", "$order $maxY")
    }
}