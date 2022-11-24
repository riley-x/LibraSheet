package com.example.librasheet.ui.categories

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ExpandLess
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.components.RowDivider
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.preview.previewExpenseCategories
import com.example.librasheet.viewModel.preview.previewIncomeCategories

@Composable
fun CategoryRow(
    category: CategoryUi,
    modifier: Modifier = Modifier,
    filterZeros: Boolean = false,
    expanded: MutableTransitionState<Boolean> = remember { MutableTransitionState(false) },
    onExpand: (Boolean) -> Unit = { expanded.targetState = it },
    content: @Composable RowScope.(CategoryUi) -> Unit = { },
    subRow: @Composable ColumnScope.(Int, CategoryUi) -> Unit = { index, cat ->
        CategorySubRow(
            category = cat,
            indicatorColor = category.color.copy(alpha = 0.5f),
            last = index == category.subCategories.lastIndex,
        ) {
            content(cat)
        }
    },
) {
    val hasExpanded =
        if (filterZeros) category.subCategories.any { it.value != 0f }
        else category.subCategories.isNotEmpty()

    Surface(modifier) {
        Column {
            ColorCodedRow(
                color = category.color,
            ) {
                Text(category.name)
                if (hasExpanded) {
                    IconButton(onClick = { onExpand(!expanded.targetState) }) {
                        if (expanded.targetState) Icon(
                            imageVector = Icons.Sharp.ExpandLess,
                            contentDescription = null
                        )
                        else Icon(imageVector = Icons.Sharp.ExpandMore, contentDescription = null)
                    }
                }
                content(category)
            }

            if (hasExpanded) {
                AnimatedVisibility(
                    visibleState = expanded,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        category.subCategories.forEachIndexed { index, cat ->
                            if (!filterZeros || cat.value != 0f) {
                                subRow(index, cat)
                                // It's important that the index passed here is the full list's index
                                // for reordering!
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Column {
                CategoryRow(
                    category = previewIncomeCategories[0]
                )
                RowDivider()
                CategoryRow(
                    category = previewExpenseCategories[0],
                    expanded = remember { MutableTransitionState(true) }
                )
            }

        }
    }
}
