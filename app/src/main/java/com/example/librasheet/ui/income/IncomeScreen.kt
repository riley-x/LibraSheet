package com.example.librasheet.ui.income

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.TransactionCategory
import com.example.librasheet.viewModel.preview.*


private val tabs = listOf("Categories", "History")


@Composable
fun IncomeScreen(
    categories: SnapshotStateList<TransactionCategory>,
    historyAxes: State<AxesState>,
    history: State<StackedLineGraphValues>,
    historyDates: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onCategoryClick: (TransactionCategory) -> Unit = { },
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var hoverText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        HeaderBar(title = "Income") {
            Spacer(Modifier.weight(10f))
            Text(hoverText, textAlign = TextAlign.End)
        }

        LazyColumn {
            item("graphic") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> PieChart(
                            values = categories,
                            modifier = Modifier
                                .padding(start = 30.dp, end = 30.dp)
                        )
                        else -> StackedLineGraph(
                            axes = historyAxes,
                            history = history,
                            modifier = Modifier.fillMaxSize()
                        ) { isHover, loc ->
                            hoverText = if (isHover)
                                formatDollar(history.value.first().second[loc]) + "\n" + historyDates[loc]
                                else ""
                        }
                    }
                }
            }

            item("selector") {
                DialSelector(
                    selectedIndex = selectedTab,
                    labels = tabs,
                    onSelection = { selectedTab = it },
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                )
            }

            itemsIndexed(categories) { index, category ->
                if (index > 0) RowDivider()

                CategoryRow(
                    category = category,
                    modifier = Modifier
                        .clickable { onCategoryClick(category) }
                )
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
            IncomeScreen(
                categories = previewIncomeCategories,
                historyAxes = previewStackedLineGraphAxes,
                history = previewStackedLineGraph,
                historyDates = previewEmptyStringList,
            )
        }
    }
}