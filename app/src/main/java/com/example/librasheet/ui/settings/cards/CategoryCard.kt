package com.example.librasheet.ui.settings.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme





@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    toEditCategories: () -> Unit = { },
    toCategoryRules: (income: Boolean) -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            CardTitle(title = "Categories")
            CardRowDivider(color = MaterialTheme.colors.primary)
            ClickableRow("Edit", toEditCategories)
            CardRowDivider()
            ClickableRow("Income Rules") { toCategoryRules(true) }
            CardRowDivider()
            ClickableRow("Expense Rules") { toCategoryRules(false) }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            CategoryCard()
        }
    }
}