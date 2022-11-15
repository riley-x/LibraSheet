package com.example.librasheet.viewModel

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.example.librasheet.data.CategoryData
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.find
import com.example.librasheet.viewModel.dataClasses.toUi
import kotlinx.coroutines.CoroutineScope

/**
 * This class contains the UI state for one cash flow screen.
 */
class CashFlowModel (
    private val data: CategoryData,
    private val dataIndex: Int, // 0 for income, 1 for expense
) {
    /** Pie chart **/
    val pie = mutableStateListOf<CategoryUi>()
    val pieRange = mutableStateOf(CategoryTimeRange.ONE_MONTH)

    /** Expanded state of each row. This is needed here since lots of bugs  occur if you try to put
     * it inside the LazyColumn::items. Index with the full category name. **/
    val isExpanded = mutableStateMapOf<String, MutableTransitionState<Boolean>>()

    fun load() {
        pie.clear()
        val amounts = when(pieRange.value) {
            CategoryTimeRange.ONE_MONTH -> data.currentMonth
            CategoryTimeRange.ONE_YEAR -> data.yearAverage
            CategoryTimeRange.ALL -> data.allAverage
        }
        val key = if (dataIndex == 0) incomeKey else expenseKey
        pie.add(
            CategoryUi(
                key = 0,
                id = CategoryId("Uncategorized"),
                color = Color(0xFF004940),
                value = amounts[key]?.toFloatDollar() ?: 0f,
            )
        )
        pie.addAll(data.all[dataIndex].subCategories.map { it.toUi(amounts) })
    }

    @Callback
    fun setPieRange(range: CategoryTimeRange) {
        if (pieRange.value == range) return
        load()
    }
}