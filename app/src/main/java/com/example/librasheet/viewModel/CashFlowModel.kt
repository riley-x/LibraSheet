package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.stackedLineGraphValues
import com.example.librasheet.data.toDoubleDollar
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

    fun isCustom() =
        (tab.value == 0 && pieRange.value == CategoryTimeRange.CUSTOM) ||
        (tab.value == 1 && historyRange.value == HistoryTimeRange.CUSTOM)

    fun customRangeDescription(): String {
        return if (!isCustom()) ""
        else if (customRangeStart.value == customRangeEnd.value) {
            formatDateInt(customRangeStart.value, "MMM yyyy")
        } else {
            val start = formatDateInt(customRangeStart.value, "MMM ''yy")
            val end = formatDateInt(customRangeEnd.value, "MMM ''yy")
            "From: $start\nTo: $end"
        }
    }
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
    private val updateDependencies: (Dependency) -> Unit = { },
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

    /** List of categories with totals displayed below the graphic. The averages list doubles as the
     * values used for the pie chart **/
    val pie = mutableStateListOf<CategoryUi>()
    val categoryTotals = mutableStateListOf<CategoryUi>()

    /** History graph. We cache the full history below and create the graph state by slicing **/
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
        } else scope.launch {
            loadPie()
            loadFullHistory()
            loadHistory()
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
        val customChanged = CashFlowCommonState.pieRange.value == CategoryTimeRange.CUSTOM
                && (currentCustomRangeStart != CashFlowCommonState.customRangeStart.value
                || currentCustomRangeEnd != CashFlowCommonState.customRangeEnd.value)
        if (customChanged || currentPieRange != CashFlowCommonState.pieRange.value) {
            loadPie()
        }
        if (customChanged || currentHistoryRange != CashFlowCommonState.historyRange.value) {
            loadHistory()
        }
    }

    private fun loadUiList(target: SnapshotStateList<CategoryUi>, amounts: Map<Long, Double>) {
        target.clear()
        target.addAll(parentCategory.subCategories.map { it.toUi(amounts, multiplier) })

        val parentValue = multiplier * amounts.getOrDefault(parentCategory.key, 0.0)
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
        scope.launch {
            val amounts = when (currentPieRange) {
                CategoryTimeRange.ONE_MONTH -> data.currentMonth
                CategoryTimeRange.ONE_YEAR -> data.yearAverage
                CategoryTimeRange.ALL -> data.allAverage
                CategoryTimeRange.CUSTOM -> {
                    currentCustomRangeStart = CashFlowCommonState.customRangeStart.value
                    currentCustomRangeEnd = CashFlowCommonState.customRangeEnd.value
                    withContext(Dispatchers.IO) {
                        data.historyDao.getAverages(currentCustomRangeStart, currentCustomRangeEnd)
                    }
                }
            }
            loadUiList(pie, amounts)
        }
    }

    private fun loadTotalsList() {
        scope.launch {
            val amounts = when(currentHistoryRange) {
                HistoryTimeRange.ONE_YEAR -> data.yearTotal
                HistoryTimeRange.FIVE_YEARS -> data.fiveYearTotal
                HistoryTimeRange.ALL -> data.allTotal
                HistoryTimeRange.CUSTOM -> {
                    currentCustomRangeStart = CashFlowCommonState.customRangeStart.value
                    currentCustomRangeEnd = CashFlowCommonState.customRangeEnd.value
                    withContext(Dispatchers.IO) {
                        data.historyDao.getTotals(currentCustomRangeStart, currentCustomRangeEnd)
                            .mapValues { it.value.toDoubleDollar() }
                    }
                }
            }
            loadUiList(categoryTotals, amounts)
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

    private fun getCustomRangeIndices(): Pair<Int, Int> {
        var startIndex = -1
        for ((index, date) in data.historyDates.withIndex()) {
            if (date == currentCustomRangeStart) startIndex = index
            if (date == currentCustomRangeEnd) return Pair(startIndex, index + 1)
                // make sure there's no else if here, since could have end == start
        }
        return Pair(0, 0)
    }

    /**
     * Safe version of List.subList() that clamps [start] and [end] to the list range.
     * The returned list is therefore not necessarily [end] - [start] long.
     */
    private fun <T> List<T>.safeSublist(start: Int, end: Int): List<T> {
        var newStart = start
        if (start < 0) newStart = 0
        if (start >= size) return emptyList()

        var newEnd = end
        if (end > size) newEnd = size
        if (end <= 0) return emptyList()

        if (newEnd < newStart) return emptyList()
        return subList(newStart, newEnd)
    }

    private fun loadHistory() {
        currentHistoryRange = CashFlowCommonState.historyRange.value
        loadTotalsList()
        if (fullHistory.isEmpty()) return

        val range = when (currentHistoryRange) {
            HistoryTimeRange.ONE_YEAR -> Pair(fullDates.size - 12, fullDates.size)
            HistoryTimeRange.FIVE_YEARS -> Pair(fullDates.size - 60, fullDates.size)
            HistoryTimeRange.ALL -> Pair(0, fullDates.size)
            HistoryTimeRange.CUSTOM -> {
                currentCustomRangeStart = CashFlowCommonState.customRangeStart.value
                currentCustomRangeEnd = CashFlowCommonState.customRangeEnd.value
                getCustomRangeIndices()
            }
        }
        val intDates = data.historyDates.safeSublist(range.first, range.second)
        Log.d("Libra/CashFlowModel/loadHistory", "range: $range, intDates: $intDates")
        if (intDates.isEmpty()) return

        dates.clear()
        dates.addAll(fullDates.safeSublist(range.first, range.second))

        history.values.clear()
        fullHistory.mapTo(history.values) {
            it.copy(second = it.second.safeSublist(range.first, range.second))
        }

        // We only need to look at [0] because that's the top stack
        val onlyOneEntry = history.values[0].second.size == 1
        val maxY = history.values[0].second.maxOrNull()?.toFloat() ?: return
        val ticksX = autoMonthTicks(intDates.first(), intDates.last(), graphTicksX)
        val (ticksY, order) = autoYTicksWithOrder(0f, maxY, graphTicksY)
        val axes = AxesState(
            ticksY = ticksY,
            ticksX = ticksX,
            minY = 0f,
            maxY = maxY + maxY * graphYPad,
            minX = if (onlyOneEntry) -0.5f else 0f,
            maxX = if (onlyOneEntry) 0.5f else dates.lastIndex.toFloat(),
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
    }

    @Callback
    fun setHistoryRange(range: HistoryTimeRange) {
        if (range != HistoryTimeRange.CUSTOM && CashFlowCommonState.historyRange.value == range) return
        CashFlowCommonState.historyRange.value = range
        loadHistory()
    }

    @Callback
    fun changeTab(tab: Int) { // TODO replace with enum
        CashFlowCommonState.tab.value = tab
    }

    @Callback
    fun reorder(parentId: String, startIndex: Int, endIndex: Int) {
        if (parentId == parentCategory.id.fullName) {
            categoryTotals.add(endIndex, categoryTotals.removeAt(startIndex))
            pie.add(endIndex, pie.removeAt(startIndex))
        }
        data.reorder(parentId, startIndex, endIndex)
        updateDependencies(Dependency.CATEGORY)
    }
}