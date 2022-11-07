package com.example.librasheet.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.preview.previewIncomeCategories

@Composable
fun CategorySubRow(
    category: Category,
    indicatorColor: Color,
    last: Boolean,
    modifier: Modifier = Modifier,
    colorRowModifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = { },
) {
    val dividerColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)

    Row(modifier) {
        ComponentIndicatorLine(
            last = last,
            color = indicatorColor,
            modifier = Modifier
                .padding(start = libraRowHorizontalPadding + 1.dp)
                .size(width = 22.dp, height = libraRowHeight)
        )
        ColorCodedRow(
            color = category.color,
            horizontalPadding = 0.dp,
            modifier = colorRowModifier
                .padding(end = libraRowHorizontalPadding)
                .rowDivider(padding = 0.dp, color = dividerColor)
        ) {
            Text(category.name)
            content()
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