package com.example.librasheet.ui.cashFlow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.selectors.ButtonGroup
import com.example.librasheet.ui.components.selectors.GraphSelector
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.graphing.PieChartFiltered
import com.example.librasheet.ui.graphing.StackedLineGraph
import com.example.librasheet.ui.graphing.StackedLineGraphState
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.categoryTimeRanges
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.historyTimeRanges
import com.example.librasheet.viewModel.preview.*

@Composable
fun CashFlowGraphic(
    selectedTab: MutableState<Int>,
    tabs: ImmutableList<String>,
    categories: SnapshotStateList<CategoryUi>,
    history: StackedLineGraphState,
    historyDates: SnapshotStateList<String>,
    categoryTimeRange: State<CategoryTimeRange>,
    historyTimeRange: State<HistoryTimeRange>,
    modifier: Modifier = Modifier,
    onSelection: (Int) -> Unit = { },
    updateHoverText: (String) -> Unit = { },
    onCategoryTimeRange: (CategoryTimeRange) -> Unit = { },
    onHistoryTimeRange: (HistoryTimeRange) -> Unit = { },
) {
    GraphSelector(
        selectedTab = selectedTab,
        tabs = tabs,
        onSelection = onSelection,
        modifier = modifier
            .fillMaxWidth()
    ) { targetState ->
        when (targetState) {
            0 -> Column {
                PieChartFiltered(
                    values = categories,
                    modifier = Modifier
                        .height(300.dp)
                        .padding(start = 30.dp, end = 30.dp)
                        .fillMaxWidth()
                )
                ButtonGroup(
                    options = categoryTimeRanges,
                    currentSelection = categoryTimeRange,
                    onSelection = onCategoryTimeRange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 5.dp)
                )
            }
            else -> Column {
                StackedLineGraph(
                    state = history,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) { isHover, loc ->
                    val hoverText = if (isHover)
                        formatDollar(history.values.first().second[loc]) + "\n" + historyDates[loc]
                    else ""
                    updateHoverText(hoverText)
                }
                ButtonGroup(
                    options = historyTimeRanges,
                    currentSelection = historyTimeRange,
                    onSelection = onHistoryTimeRange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Preview(
    widthDp = 360,
)
@Composable
private fun Preview() {
    val tab = remember { mutableStateOf(0) }
    LibraSheetTheme {
        Surface {
            CashFlowGraphic(
                selectedTab = tab,
                tabs = ImmutableList(listOf("Categories", "History")),
                categories = previewIncomeCategories,
                history = previewStackedLineGraphState,
                historyDates = previewEmptyStringList,
                categoryTimeRange = previewIncomeCategoryTimeRange,
                historyTimeRange = previewIncomeHistoryTimeRange,
            )
        }
    }
}