package com.example.librasheet.ui.cashFlow

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.example.librasheet.data.entity.CategoryId
import com.example.librasheet.data.entity.ignoreKey
import com.example.librasheet.data.entity.toCategoryId
import com.example.librasheet.ui.categories.*
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.HistoryTimeRange
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.*


private val tabs = ImmutableList(listOf("Categories", "History"))


@Composable
fun CashFlowScreen(
    parentCategory: CategoryId,
    categories: SnapshotStateList<CategoryUi>,
    expanded: SnapshotStateMap<String, MutableTransitionState<Boolean>>,
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
    onReorder: (parentId: String, startIndex: Int, endIndex: Int) -> Unit = { _, _, _ -> },
) {
    var hoverText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        HeaderBar(
            title = parentCategory.name,
            backArrow = headerBackArrow,
            onBack = onBack,
            modifier = Modifier.zIndex(1f),
        ) {
            Spacer(Modifier.weight(10f))
            Text(hoverText, textAlign = TextAlign.End)
        }

        DragHost {
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

                val startIndex = categories.indexOfFirst { it.value > 0 }
                /** Warning the lazy column must be keyed with the index. Keying the lazy column like
                 * `key = { _, it -> it.id.fullName }` messes up the passed index into DragToReorderTarget,
                 * which won't be reset. **/
                itemsIndexed(categories) { index, category ->
                    if (category.value > 0) {
                        CategoryDragRow(
                            category = category,
                            group = parentCategory.fullName,
                            index = index,
                            startIndex = startIndex,
                            enabled = category.key != ignoreKey, // "Uncategorized" is added to the end if it exists, but don't want to drag this
                            expanded = expanded,
                            onReorder = onReorder,
                            content = { cat ->
                                Spacer(modifier = Modifier.weight(10f))
                                Text(formatDollar(cat.value))
                            },
                            subContent = { cat ->
                                Spacer(modifier = Modifier.weight(10f))
                                Text(formatDollar(cat.value))
                            },
                            modifier = Modifier.clickable {
                                if (category.subCategories.isNotEmpty()) onCategoryClick(category)
                            }
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
                parentCategory = "Income".toCategoryId(),
                categories = previewIncomeCategories,
                expanded = previewExpanded,
                history = previewStackedLineGraphState,
                historyDates = previewEmptyStringList,
                categoryTimeRange = previewIncomeCategoryTimeRange,
                historyTimeRange = previewIncomeHistoryTimeRange,
            )
        }
    }
}