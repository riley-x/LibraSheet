package com.example.librasheet.viewModel

import android.util.Log
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.librasheet.data.*
import com.example.librasheet.data.entity.*
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
 * @property pieRangeDates (start, end) of current pie range being displayed, where end is inclusive.
 * These are IntDates in YYYYMM00 form, indicating the last day of month MM-1.
 * @property historyRangeDates (start, end) of current history range being displayed, where end is inclusive.
 * These are IntDates in YYYYMM00 form, indicating the last day of month MM-1.
 */
object CashFlowCommonState {
    /** Current tab being displayed (pie + averages, history + totals). **/
    val tab = mutableStateOf(0)
    val pieRange = mutableStateOf(CategoryTimeRange.ONE_MONTH)
    val historyRange = mutableStateOf(HistoryTimeRange.ONE_YEAR)
    val pieRangeDates = mutableStateOf(Pair(0, 0))
    val historyRangeDates = mutableStateOf(Pair(0, 0))

    fun rangeDescription(): String {
        val dates = if (tab.value == 0) pieRangeDates.value else historyRangeDates.value
        return if (dates.first == dates.second) {
            formatDateInt(dates.first, "MMM yyyy")
        } else {
            val start = formatDateInt(dates.first, "MMM ''yy")
            val end = formatDateInt(dates.second, "MMM ''yy")
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
     * but we load them lazily. So we need to know when the current values are stale. **/
    private var currentTab = CashFlowCommonState.tab.value
    private var currentPieRange = Pair(0, 0)
    private var currentHistoryRange = Pair(0, 0)

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
        if (loadOnInit) init(data.find(categoryId).first)
    }

    private fun init(category: Category = parentCategory) {
        Log.d("Libra/CashFlowModel/load", "category=${category.id}")
        parentCategory = category

        if (parentCategory.key == 0L) {
            pie.clear()
            fullHistory = emptyList()
            fullDates = emptyList()
            history.values.clear()
            dates.clear()
        } else scope.launch {
            setPieRange(CashFlowCommonState.pieRange.value)
            loadFullHistory()
            setHistoryRange(CashFlowCommonState.historyRange.value)
        }
    }

    /** We want all the cash flow screens to have the same tab and range, but we load lazily. This
     * function is called via a launched effect when the model is loaded by the composable. **/
    fun resyncState() {
        // this function can be called while loadFullHistory() is suspended in init(). Make sure to
        // not prematurely load the UI elements.
        if (fullHistory.isEmpty()) return

        Log.d("Libra/CashFlowModel/resyncState", "${parentCategory.id}" +
                " $currentTab-${CashFlowCommonState.tab.value}" +
                " ${CashFlowCommonState.pieRange.value}: $currentPieRange-${CashFlowCommonState.pieRangeDates.value}" +
                " ${CashFlowCommonState.historyRange.value}: $currentHistoryRange-${CashFlowCommonState.historyRangeDates.value}")
        if (currentPieRange != CashFlowCommonState.pieRangeDates.value) {
            loadPie()
        }
        if (currentHistoryRange != CashFlowCommonState.historyRangeDates.value) {
            loadHistory()
        }
    }

    /** Helper function that loads either of the tab's UI lists. This creates the CategoryUi objects
     * from a given map of values. **/
    private fun loadUiList(target: SnapshotStateList<CategoryUi>, amounts: Map<Long, Double>) {
        target.clear()
        target.addAll(parentCategory.subCategories.map { it.toUi(amounts, multiplier) })

        /** Remember transactions don't have to be categorized with a subcategory, and can be
         * categorized with only the parent category. So create an extra UI element to display these.
         * However we don't use [Category.toUi] so we can exclude the subcategories.
         **/
        val parentValue = multiplier * amounts.getOrDefault(parentCategory.key, 0.0)
        if (parentValue > 0f) {
            target.add(
                CategoryUi(
                    category = parentCategory, // setting this correctly enables click-to-search-transactions
                    key = ignoreKey, // CashFlowScreen checks this to not allow dragging
                    id = CategoryId("Uncategorized"), // displayed name in CashFlowScreen
                    color = parentCategory.color,
                    value = parentValue,
                )
            )
        }
    }

    /** Fully and always loads the UI elements for the pie chart (averages) tab based on the current
     * range set in [CashFlowCommonState]. Updates [currentPieRange] to match. **/
    private fun loadPie() {
        currentPieRange = CashFlowCommonState.pieRangeDates.value
        scope.launch {
            val amounts = when (CashFlowCommonState.pieRange.value) {
                CategoryTimeRange.ONE_MONTH -> data.currentMonth
                CategoryTimeRange.ONE_YEAR -> data.yearAverage
                CategoryTimeRange.ALL -> data.allAverage
                CategoryTimeRange.CUSTOM -> withContext(Dispatchers.IO) {
                    data.historyDao.getAverages(currentPieRange.first, currentPieRange.second)
                }
            }
            loadUiList(pie, amounts)
        }
    }

    /** Loads the UI elements for the category list in the history (totals) tab based on the current
     * range set in [CashFlowCommonState]. Updates [currentHistoryRange] to match. See [loadHistory]
     * for the full load of the history tab. **/
    private fun loadTotalsList() {
        scope.launch {
            val amounts = when(CashFlowCommonState.historyRange.value) {
                HistoryTimeRange.ONE_YEAR -> data.yearTotal
                HistoryTimeRange.FIVE_YEARS -> data.fiveYearTotal
                HistoryTimeRange.ALL -> data.allTotal
                HistoryTimeRange.CUSTOM -> withContext(Dispatchers.IO) {
                    data.historyDao.getTotals(
                        CashFlowCommonState.historyRangeDates.value.first,
                        CashFlowCommonState.historyRangeDates.value.second
                    ).mapValues { it.value.toDoubleDollar() }
                }
            }
            loadUiList(categoryTotals, amounts)
        }
    }

    /** Loads the entire category history into memory. Cached for updating the UI elements. **/
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

    /** Helper function for [loadHistory]. Gets the index into [data.historyDates] for [currentHistoryRange] **/
    private fun getCustomRangeIndices(): Pair<Int, Int> {
        var startIndex = -1
        for ((index, date) in data.historyDates.withIndex()) {
            if (date == currentHistoryRange.first) startIndex = index
            if (date == currentHistoryRange.second) return Pair(startIndex, index + 1)
                // make sure there's no else if here, since could have end == start
        }
        return Pair(0, 0)
    }

    /** Loads all the UI elements for the history tab. **/
    private fun loadHistory() {
        currentHistoryRange = CashFlowCommonState.historyRangeDates.value
        loadTotalsList()
        if (fullHistory.isEmpty()) return

        val range = when (CashFlowCommonState.historyRange.value) {
            HistoryTimeRange.ONE_YEAR -> Pair(fullDates.size - 13, fullDates.size)   // Extra month to include current month.
            HistoryTimeRange.FIVE_YEARS -> Pair(fullDates.size - 61, fullDates.size) // Agrees also with dates set in [setHistoryRange], which uses offsets from [data.lastMonthEnd]
            HistoryTimeRange.ALL -> Pair(0, fullDates.size)
            HistoryTimeRange.CUSTOM -> getCustomRangeIndices()
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

    /** Sets the current pie tab range, and reloads the UI elements if necessary. **/
    @Callback
    fun setPieRange(range: CategoryTimeRange) {
        CashFlowCommonState.pieRange.value = range
        when (range) {
            CategoryTimeRange.ONE_MONTH -> {
                CashFlowCommonState.pieRangeDates.value = Pair(data.thisMonthEnd, data.thisMonthEnd)
            }
            CategoryTimeRange.ONE_YEAR -> {
                val start = maxOf(data.firstMonthEnd, data.lastYearEnd)
                CashFlowCommonState.pieRangeDates.value = Pair(start, data.lastMonthEnd)
            }
            CategoryTimeRange.ALL -> {
                CashFlowCommonState.pieRangeDates.value = Pair(data.firstMonthEnd, data.lastMonthEnd)
            }
            CategoryTimeRange.CUSTOM -> Unit // Already set by [setCustomTimeRange]
        }
        if (currentPieRange != CashFlowCommonState.pieRangeDates.value) loadPie()
    }

    /** Sets the current history tab range, and reloads the UI elements if necessary. **/
    @Callback
    fun setHistoryRange(range: HistoryTimeRange) {
        CashFlowCommonState.historyRange.value = range
        when (range) {
            HistoryTimeRange.ONE_YEAR -> {
                val start = maxOf(data.firstMonthEnd, data.lastYearEnd)
                CashFlowCommonState.historyRangeDates.value = Pair(start, data.thisMonthEnd)
            }
            HistoryTimeRange.FIVE_YEARS -> {
                val start = maxOf(data.firstMonthEnd, data.lastMonthEnd.addYears(-5))
                CashFlowCommonState.historyRangeDates.value = Pair(start, data.thisMonthEnd)
            }
            HistoryTimeRange.ALL -> {
                CashFlowCommonState.historyRangeDates.value = Pair(data.firstMonthEnd, data.thisMonthEnd)
            }
            HistoryTimeRange.CUSTOM -> Unit // Already set by [setCustomTimeRange]
        }
        if (currentHistoryRange != CashFlowCommonState.historyRangeDates.value) loadHistory()
    }

    /** Callback from custom time range dialog. Sets the range of the current tab to the given values **/
    @Callback
    fun setCustomTimeRange(start: Int, end: Int) {
        if (CashFlowCommonState.tab.value == 0) {
            CashFlowCommonState.pieRangeDates.value = Pair(start, end)
            setPieRange(CategoryTimeRange.CUSTOM)
        } else {
            CashFlowCommonState.historyRangeDates.value = Pair(start, end)
            setHistoryRange(HistoryTimeRange.CUSTOM)
        }
    }

    @Callback
    fun changeTab(tab: Int) { // TODO replace with enum
        CashFlowCommonState.tab.value = tab
    }

    @Callback
    fun reorder(parentId: String, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return
        if (parentId == parentCategory.id.fullName) {
            categoryTotals.add(endIndex, categoryTotals.removeAt(startIndex))
            pie.add(endIndex, pie.removeAt(startIndex))
        }
        data.reorder(parentId, startIndex, endIndex)
        updateDependencies(Dependency.CATEGORY)
    }
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