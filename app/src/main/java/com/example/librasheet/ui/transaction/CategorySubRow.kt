package com.example.librasheet.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.components.libraRowHeight
import com.example.librasheet.ui.components.libraRowHorizontalPadding
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.TransactionCategory
import com.example.librasheet.viewModel.preview.previewIncomeCategories

@Composable
fun CategorySubRow(
    category: TransactionCategory,
    last: Boolean,
    modifier: Modifier = Modifier,
) {
    val dividerColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(libraRowHeight)
            .fillMaxWidth()
            .padding(horizontal = libraRowHorizontalPadding)
            .drawBehind { // Can't use Divider because that adds a space between the ComponentIndicatorLines
                val strokeWidth = 1.dp.toPx()
                val y = strokeWidth / 2
                drawLine(
                    color = dividerColor,
                    start = Offset(52.dp.toPx(), y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        ComponentIndicatorLine(
            last = last,
            color = category.color.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(start = 26.dp, end = 4.dp)
                .size(width = 22.dp, height = libraRowHeight)
        )

        Text(category.name)
        Spacer(modifier = Modifier.weight(10f))
        Text(formatDollar(category.value))
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Column {
                CategorySubRow(category = previewIncomeCategories[0], last = false)
                CategorySubRow(category = previewIncomeCategories[0], last = true)
            }
        }
    }
}