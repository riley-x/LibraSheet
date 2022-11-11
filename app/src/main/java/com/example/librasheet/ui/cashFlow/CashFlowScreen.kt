package com.example.librasheet.ui.cashFlow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.librasheet.ui.categories.CategoryRow
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.categoryTimeRanges
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.historyTimeRanges
import com.example.librasheet.viewModel.preview.*


private val tabs = ImmutableList(listOf("Categories", "History"))


@Composable
fun CashFlowScreen(
    title: String,
    categories: SnapshotStateList<CategoryUi>,
    history: StackedLineGraphState,
    historyDates: SnapshotStateList<String>,
    categoryTimeRange: State<CategoryTimeRange>,
    historyTimeRange: State<HistoryTimeRange>,
    modifier: Modifier = Modifier,
    headerBackArrow: Boolean = false,
    onBack: () -> Unit = { },
    onCategoryClick: (CategoryUi) -> Unit = { },
    onCategoryTimeRange: (CategoryTimeRange) -> Unit = { },
    onHistoryTimeRange: (HistoryTimeRange) -> Unit = { },
) {
    var hoverText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        HeaderBar(
            title = title,
            backArrow = headerBackArrow,
            onBack = onBack,
            modifier = Modifier.zIndex(1f),
        ) {
            Spacer(Modifier.weight(10f))
            Text(hoverText, textAlign = TextAlign.End)
        }

        DragHost {
            /** Note you have to draw the dividers with the composables, because the size of each
             * category row can be different if they're expanded. So it doesn't make sense to have
             * fixed dividers like in the EditAccountScreen. **/
            val dividerColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)

            LazyColumn {
                item("graphic") {
                    CashFlowGraphic(
                        tabs = tabs,
                        categories = categories,
                        history = history,
                        historyDates = historyDates,
                        categoryTimeRange = categoryTimeRange,
                        historyTimeRange = historyTimeRange,
                        updateHoverText = { hoverText = it },
                        onCategoryTimeRange = onCategoryTimeRange,
                        onHistoryTimeRange = onHistoryTimeRange,
                    )
                }

                val firstIndex = categories.indexOfFirst { it.value > 0 }
                itemsIndexed(categories) { index, category ->
                    if (category.value > 0) {
                        if (index > firstIndex) RowDivider()

                        CategoryRow(
                            category = category,
                            content = {
                                Spacer(modifier = Modifier.weight(10f))
                                Text(formatDollar(it.value))
                            },
                            modifier = Modifier
                                .clickable { if (category.subCategories.isNotEmpty()) onCategoryClick(category) }
                        )
                    }
                }
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