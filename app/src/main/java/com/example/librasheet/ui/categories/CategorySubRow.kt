package com.example.librasheet.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.preview.previewIncomeCategories

@Composable
fun CategorySubRow(
    category: CategoryUi,
    indicatorColor: Color,
    last: Boolean,
    modifier: Modifier = Modifier,
    dragIndex: Int = -1,
    dragGroup: String = "",
    onDragEnd: (group: String, startIndex: Int, endIndex: Int) -> Unit = { _, _, _ -> },
    dividerColor: Color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
    colorRowModifier: Modifier = Modifier,
    content: @Composable RowScope.(CategoryUi) -> Unit = { },
) {
    Row(modifier) {
        ComponentIndicatorLine(
            last = last,
            color = indicatorColor,
            modifier = Modifier
                .padding(start = libraRowHorizontalPadding + 1.dp)
                .size(width = 22.dp, height = libraRowHeight)
        )
        DragToReorderTarget(
            index = dragIndex,
            group = dragGroup,
            enabled = dragIndex != -1,
            onDragEnd = onDragEnd,
        ) { dragScope ->
            ColorCodedRow(
                color = category.color,
                horizontalPadding = 0.dp,
                modifier = colorRowModifier
                    .padding(end = libraRowHorizontalPadding)
                    .rowDivider(padding = 0.dp, color = dividerColor, enabled = !dragScope.isTarget(dragGroup, dragIndex))
            ) {
                Text(category.name)
                content(category)
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
                CategorySubRow(
                    category = previewIncomeCategories[0],
                    indicatorColor = Color.Green,
                    last = false
                )
                CategorySubRow(
                    category = previewIncomeCategories[0],
                    indicatorColor = Color.Green,
                    last = true
                )
            }
        }
    }
}