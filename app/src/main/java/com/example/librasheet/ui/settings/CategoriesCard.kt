package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme





@Composable
fun CategoriesCard(
    modifier: Modifier = Modifier,
    toEditCategories: () -> Unit = { },
    toCategoryRules: () -> Unit = { },
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
            ClickableRow("Rules", toCategoryRules)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            CategoriesCard()
        }
    }
}