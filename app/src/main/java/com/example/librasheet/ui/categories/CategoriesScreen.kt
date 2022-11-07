package com.example.librasheet.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowDivider
import com.example.librasheet.ui.components.RowTitle
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.settings.CardTitle
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.preview.*
import kotlin.math.exp

@Composable
fun CategoriesScreen(
    incomeCategories: SnapshotStateList<Category>,
    expenseCategories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onCategoryClick: (Category) -> Unit = { },
) {

    fun LazyListScope.categoryItems(list: SnapshotStateList<Category>) {
        itemsIndexed(list) { index, category ->
            if (index > 0) RowDivider()

            CategoryRow(
                category = category,
                subRowContent = {
                    Spacer(modifier = Modifier.weight(10f))
                    Text(formatDollar(it.value))
                },
                modifier = Modifier
                    .clickable { onCategoryClick(category) }
            ) {
                Spacer(modifier = Modifier.weight(10f))
                Text(formatDollar(category.value))
            }
        }
    }

    Column(modifier) {
        HeaderBar(title = "Categories", backArrow = true, onBack = onBack)
        
        LazyColumn {
            item("income_title") {
                RowTitle(title = "Income")
            }
            categoryItems(incomeCategories)
            item("expense_title") {
                RowTitle(title = "Expense", modifier = Modifier.padding(top = 20.dp))
            }
            categoryItems(expenseCategories)
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
            CategoriesScreen(
                incomeCategories = previewIncomeCategories,
                expenseCategories = previewIncomeCategories,
            )
        }
    }
}