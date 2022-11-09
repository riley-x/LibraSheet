package com.example.librasheet.ui.categories

import androidx.compose.animation.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.preview.previewIncomeCategories

@Composable
fun CategoryRow(
    category: Category,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpand: (Boolean) -> Unit = {  },
    content: @Composable RowScope.(Category) -> Unit = { },
    subRow: @Composable ColumnScope.(Int, Category) -> Unit = { index, cat ->
        CategorySubRow(
            category = cat,
            indicatorColor = category.color.copy(alpha = 0.5f),
            last = index == category.subCategories.lastIndex,
        ) {
            content(cat)
        }
    },
) {
    Surface(modifier) {
        Column {
            ColorCodedRow(
                color = category.color,
            ) {
                Text(category.name)
                if (category.subCategories.isNotEmpty()) {
                    IconButton(onClick = { onExpand(!expanded) }) {
                        if (expanded) Icon(
                            imageVector = Icons.Sharp.ExpandLess,
                            contentDescription = null
                        )
                        else Icon(imageVector = Icons.Sharp.ExpandMore, contentDescription = null)
                    }
                }
                content(category)
            }

            if (category.subCategories.isNotEmpty()) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        category.subCategories.forEachIndexed { index, cat ->
                            subRow(index, cat)
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
            CategoryRow(
                category = previewIncomeCategories[0]
            )
        }
    }
}
