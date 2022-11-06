package com.example.librasheet.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme


@Composable
private fun CategoryRow(
    name: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .cardRow()
    ) {
        Text(name, modifier = Modifier.weight(10f))
        Icon(Icons.Sharp.ArrowForwardIos, null)
    }
}


@Composable
fun CategoriesCard(
//    incomeCategories: SnapshotStateList<Category>,
//    spendingCategories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    toIncomeCategories: () -> Unit = { },
    toSpendingCategories: () -> Unit = { },
    toCategoryRules: () -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            CardTitle(title = "Categories")
            CardRowDivider(color = MaterialTheme.colors.primary)
            CategoryRow("Income", toIncomeCategories)
            CardRowDivider()
            CategoryRow("Spending", toSpendingCategories)
            CardRowDivider()
            CategoryRow("Rules", toCategoryRules)
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