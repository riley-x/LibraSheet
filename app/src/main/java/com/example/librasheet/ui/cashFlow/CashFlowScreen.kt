package com.example.librasheet.ui.cashFlow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.categories.CategoryRow
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.categoryTimeRanges
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.historyTimeRanges
import com.example.librasheet.viewModel.preview.*


private val tabs = ImmutableList(listOf("Categories", "History"))


@Composable
fun CashFlowScreen(
    title: String,
    categories: SnapshotStateList<Category>,
    history: StackedLineGraphState,
    historyDates: SnapshotStateList<String>,
    categoryTimeRange: State<CategoryTimeRange>,
    historyTimeRange: State<HistoryTimeRange>,
    modifier: Modifier = Modifier,
    headerBackArrow: Boolean = false,
    onCategoryClick: (Category) -> Unit = { },
    onCategoryTimeRange: (CategoryTimeRange) -> Unit = { },
    onHistoryTimeRange: (HistoryTimeRange) -> Unit = { },
) {
    val selectedTab = rememberSaveable { mutableStateOf(0) }
    var hoverText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        HeaderBar(title = title, backArrow = headerBackArrow) {
            Spacer(Modifier.weight(10f))
            Text(hoverText, textAlign = TextAlign.End)
        }

        LazyColumn {
            item("graphic") {
                GraphSelector(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onSelection = { selectedTab.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    when (it) {
                        0 -> Column {
                            PieChart(
                                values = categories,
                                modifier = Modifier
                                    .height(300.dp)
                                    .padding(start = 30.dp, end = 30.dp)
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
                                hoverText = if (isHover)
                                    formatDollar(history.values.first().second[loc]) + "\n" + historyDates[loc]
                                else ""
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

            itemsIndexed(categories) { index, category ->
                if (index > 0) RowDivider()

//                CategoryRow(
//                    category = category,
//                    subRowContent = { _, it ->
//                        Spacer(modifier = Modifier.weight(10f))
//                        Text(formatDollar(it.value))
//                    },
//                    modifier = Modifier
//                        .clickable { onCategoryClick(category) }
//                ) {
//                    Spacer(modifier = Modifier.weight(10f))
//                    Text(formatDollar(category.value))
//                }
            }
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
)
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            CashFlowScreen(
                title = "Income",
                categories = previewIncomeCategories,
                history = previewStackedLineGraphState,
                historyDates = previewEmptyStringList,
                categoryTimeRange = previewIncomeCategoryTimeRange,
                historyTimeRange = previewIncomeHistoryTimeRange,
            )
        }
    }
}