package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.stackedLineGraphValues
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.components.formatOrder
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.toUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object CashFlowCommonState {
    /** Current tab being displayed (pie + averages, history + totals). **/
    val tab = mutableStateOf(0)
    val pieRange = mutableStateOf(CategoryTimeRange.ONE_YEAR)
    val historyRange = mutableStateOf(HistoryTimeRange.ONE_YEAR)
    val customRangeStart = mutableStateOf(0)
    val customRangeEnd = mutableStateOf(0)
}


/**
 * This class contains the UI state for one cash flow screen, which shows the details of a single
 * parent category and its subcategories. The data is loaded for the given `categoryId` on
 * construction of the class.
 *
 * @param categoryId parent category that this model handles
 * @param loadOnInit set false for previews
 */
class CashFlowModel (
    private val scope: CoroutineScope,
    private val data: CategoryData,
    categoryId: CategoryId,
    loadOnInit: Boolean = true,
) {
    var parentCategory by mutableStateOf(Category.None)
    private val multiplier = if (categoryId.superName == incomeName) 1f else -1f

    private val graphYPad = 0.1f
    private val graphTicksX = 4
    private val graphTicksY = 6

    /** Cached state values. We want all cash flow models to be in-sync on the below state values,
     * but we load them lazily. **/
    private var currentTab = CashFlowCommonState.tab.value
    private var currentPieRange = CashFlowCommonState.pieRange.value
    private var currentHistoryRange = CashFlowCommonState.historyRange.value
    private var currentCustomRangeStart = 0
    private var currentCustomRangeEnd = 0

    /** List of categories with values displayed below the graphic. **/
    val categoryList = mutableStateListOf<CategoryUi>()

    /** Pie chart. This needs to have a separate list because the cash flow screen animates between
     * the tabs, in which case both the pie and the totals list are shown concurrently. **/
    val pie = mutableStateListOf<CategoryUi>()

    /** History graph **/
    private var fullHistory = listOf<StackedLineGraphValue>()
    private var fullDates = listOf<String>()

    val history = StackedLineGraphState()
    val dates = mutableStateListOf<String>()

    /** Expanded state of each row. This is needed here since lots of bugs occur if you try to put
     * it inside the LazyColumn::items. Index with the full category name. **/
    val isExpanded = mutableStateMapOf<String, MutableTransitionState<Boolean>>()

    init {
        if (loadOnInit) load(data.find(categoryId).first)
    }

    private fun load(category: Category = parentCategory) {
        Log.d("Libra/CashFlowModel/load", "category=${category.id}")
        parentCategory = category

        if (parentCategory.key == 0L) {
            pie.clear()
            fullHistory = emptyList()
            fullDates = emptyList()
            history.values.clear()
            dates.clear()
        } else {
            loadPie()
            loadCategoryList()
            scope.launch {
                loadFullHistory()
                loadHistory()
            }
        }
    }

    /**
     * We want all the cash flow screens to have the same tab and range, but we load lazily.
     */
    fun resyncState() {
        Log.d("Libra/CashFlowModel/resyncState", "${parentCategory.id}" +
                " $currentPieRange-${CashFlowCommonState.pieRange.value}" +
                " $currentHistoryRange-${CashFlowCommonState.historyRange.value}" +
                " $currentTab-${CashFlowCommonState.tab.value}")
        var reloadList = false
        val customChanged = CashFlowCommonState.pieRange.value == CategoryTimeRange.CUSTOM
                && (currentCustomRangeStart != CashFlowCommonState.customRangeStart.value
                || currentCustomRangeEnd != CashFlowCommonState.customRangeEnd.value)
        if (customChanged || currentPieRange != CashFlowCommonState.pieRange.value) {
            loadPie()
            reloadList = true
        }
        if (customChanged || currentHistoryRange != CashFlowCommonState.historyRange.value) {
            loadHistory()
            reloadList = true
        }
        if (reloadList || currentTab != CashFlowCommonState.tab.value) loadCategoryList() // this needs to happen after pie/history loaded
    }

    private fun loadUiList(target: SnapshotStateList<CategoryUi>, amounts: Map<Long, Float>) {
        target.clear()
        target.addAll(parentCategory.subCategories.map { it.toUi(amounts, multiplier) })

        val parentValue = multiplier * amounts.getOrDefault(parentCategory.key, 0f)
        if (parentValue > 0f) {
            target.add(
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


    private fun loadPie() {
        currentPieRange = CashFlowCommonState.pieRange.value
        val amounts = when(currentPieRange) {
            CategoryTimeRange.ONE_MONTH -> data.currentMonth
            CategoryTimeRange.ONE_YEAR -> data.yearAverage
            CategoryTimeRange.ALL -> data.allAverage
            CategoryTimeRange.CUSTOM -> data.allAverage // TODO
        }
        loadUiList(pie, amounts)
    }

    private fun loadCategoryList() {
        currentTab = CashFlowCommonState.tab.value
        when (currentTab) {
            0 -> {
                categoryList.clear()
                categoryList.addAll(pie)
            }
            1 -> {
                val amounts = when(currentHistoryRange) {
                    HistoryTimeRange.ONE_YEAR -> data.yearTotal
                    HistoryTimeRange.FIVE_YEARS -> data.fiveYearTotal
                    HistoryTimeRange.ALL -> data.allTotal
                    HistoryTimeRange.CUSTOM -> data.allTotal // TODO
                }
                loadUiList(categoryList, amounts)
            }
        }
    }


    private suspend fun loadFullHistory() {
        fullDates = data.historyDates.map {
            formatDateInt(it, "MMM yyyy")
        }

        val categoryList = parentCategory.subCategories + listOf(Category(
            key = parentCategory.key,
            id = CategoryId("Uncategorized"),
            color = parentCategory.color,
        ))

        fullHistory = withContext(Dispatchers.Default) {
            data.history.stackedLineGraphValues(categoryList, multiplier, lastSeriesIsTotal = true).first
        }
        Log.d("Libra/CashFlowModel/loadFullHistory", "$fullDates")
        Log.d("Libra/CashFlowModel/loadFullHistory", "$fullHistory")
    }


    private fun <T> List<T>.takeLastOrAll(n: Int) = if (n >= 0) takeLast(n) else this

    private fun loadHistory() {
        currentHistoryRange = CashFlowCommonState.historyRange.value
        if (fullHistory.isEmpty()) return

        val n = when (currentHistoryRange) {
            HistoryTimeRange.ONE_YEAR -> 12
            HistoryTimeRange.FIVE_YEARS -> 60
            HistoryTimeRange.ALL -> -1
            HistoryTimeRange.CUSTOM -> -1 // TODO
        }

        dates.clear()
        dates.addAll(fullDates.takeLastOrAll(n))
        val intDates = data.historyDates.takeLastOrAll(n)

        history.values.clear()
        fullHistory.mapTo(history.values) {
            it.copy(second = it.second.takeLastOrAll(n))
        }

        // We only need to look at [0] because that's the top stack
        val maxY = history.values[0].second.max()
        val ticksX = autoXTicksDiscrete(dates.size, graphTicksX) {
            formatDateInt(intDates[it], "MMM ''yy") // single quote escapes the date formatters, so need '' to place a literal quote
        }
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

    @Callback
    fun setPieRange(range: CategoryTimeRange) {
        if (range != CategoryTimeRange.CUSTOM && CashFlowCommonState.pieRange.value == range) return
        CashFlowCommonState.pieRange.value = range
        loadPie()
        loadCategoryList()
    }

    @Callback
    fun setHistoryRange(range: HistoryTimeRange) {
        if (range != HistoryTimeRange.CUSTOM && CashFlowCommonState.historyRange.value == range) return
        CashFlowCommonState.historyRange.value = range
        loadHistory()
        loadCategoryList()
    }

    @Callback
    fun changeTab(tab: Int) { // TODO replace with enum
        if (CashFlowCommonState.tab.value == tab) return
        CashFlowCommonState.tab.value = tab
        loadCategoryList()
    }

    @Callback
    fun reorder(parentId: String, startIndex: Int, endIndex: Int) {
//        viewModel.categories.reorder()
        if (parentId == parentCategory.id.fullName)
            categoryList.add(endIndex, categoryList.removeAt(startIndex))
        // TODO database, subcats
    }
}