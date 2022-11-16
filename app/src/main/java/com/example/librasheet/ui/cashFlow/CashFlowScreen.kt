package com.example.librasheet.ui.cashFlow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.example.librasheet.data.entity.ignoreKey
import com.example.librasheet.ui.categories.CategoryDragRow
import com.example.librasheet.ui.components.DragHost
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.CashFlowModel
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.previewCashFlowModel


private val tabs = ImmutableList(listOf("Monthly Averages", "Totals"))


@Composable
fun CashFlowScreen(
    state: CashFlowModel,
    modifier: Modifier = Modifier,
    headerBackArrow: Boolean = false,
    onBack: () -> Unit = { },
    onCategoryClick: (CategoryUi) -> Unit = { },
    onReorder: (parentId: String, startIndex: Int, endIndex: Int) -> Unit = { _, _, _ -> },
) {
    var hoverText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        HeaderBar(
            title = state.parentCategory.id.name,
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
                        selectedTab = state.tab,
                        tabs = tabs,
                        categories = state.pie,
                        history = state.history,
                        historyDates = state.dates,
                        categoryTimeRange = state.pieRange,
                        historyTimeRange = state.historyRange,
                        updateHoverText = { hoverText = it },
                        onCategoryTimeRange = state::setPieRange,
                        onHistoryTimeRange = state::setHistoryRange,
                        onSelection = state::changeTab,
                    )
                }

                val startIndex = state.categoryList.indexOfFirst { it.value > 0 }
                /** Warning the lazy column must be keyed with the index. Keying the lazy column like
                 * `key = { _, it -> it.id.fullName }` messes up the passed index into DragToReorderTarget,
                 * which won't be reset. **/
                itemsIndexed(state.categoryList) { index, category ->
                    if (category.value > 0) {
                        CategoryDragRow(
                            category = category,
                            group = state.parentCategory.id.fullName,
                            index = index,
                            startIndex = startIndex,
                            enabled = category.key != ignoreKey, // "Uncategorized" is added to the end if it exists, but don't want to drag this
                            expanded = state.isExpanded,
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
                state = previewCashFlowModel,
            )
        }
    }
}