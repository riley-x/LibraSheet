package com.example.librasheet.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.*


private enum class CategoryOptions(override val displayName: String): HasDisplayName {
    RENAME("Rename"),
    COLOR("Change Color"),
    ADD("Add Subcategory"),
    DELETE("Delete"),
}
private enum class SubCategoryOptions(override val displayName: String): HasDisplayName {
    RENAME("Rename"),
    COLOR("Change Color"),
    MOVE("Change Parent"),
    DELETE("Delete"),
}
private val categoryOptions = ImmutableList(CategoryOptions.values().toList())
private val subCategoryOptions = ImmutableList(SubCategoryOptions.values().toList())


@Composable
fun CategoriesScreen(
    incomeCategories: SnapshotStateList<Category>,
    expenseCategories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
//    onCategoryClick: (Category) -> Unit = { },
    onChangeName: (Category) -> Unit = { },
    onChangeColor: (String) -> Unit = { },
    onAddSubCategory: (Category) -> Unit = { },
    onMoveSubCategory: (Category) -> Unit = { },
    onDelete: (Category) -> Unit = { },
) {
    val incomeDragScope = remember { DragScope() } // Don't need state here since this is only passed into other composables.
    val expenseDragScope = remember { DragScope() } // Don't need state here since this is only passed into other composables.

    fun LazyListScope.categoryItems(list: SnapshotStateList<Category>, dragScope: DragScope) {
        itemsIndexed(list) { index, category ->
            if (index > 0) RowDivider()

            CategoryRow(
                category = category,
                modifier = Modifier.dragToReorder(dragScope = dragScope, index = index),
//                subRowModifier = { subIndex, _ -> Modifier.drag(subIndex, category.id) },
                subRowContent = { _, subCategory ->
                    Spacer(modifier = Modifier.weight(10f))
                    DropdownOptions(options = subCategoryOptions) {
                        when (it) {
                            SubCategoryOptions.RENAME -> onChangeName(subCategory)
                            SubCategoryOptions.COLOR -> onChangeColor("category_${subCategory.name}")
                            SubCategoryOptions.MOVE -> onMoveSubCategory(subCategory)
                            SubCategoryOptions.DELETE -> onDelete(subCategory)
                        }
                    }
                },
            ) {
                Spacer(modifier = Modifier.weight(10f))
                DropdownOptions(options = categoryOptions) {
                    when (it) {
                        CategoryOptions.RENAME -> onChangeName(category)
                        CategoryOptions.COLOR -> onChangeColor("category_${category.name}")
                        CategoryOptions.ADD -> onAddSubCategory(category)
                        CategoryOptions.DELETE -> onDelete(category)
                    }
                }
            }
        }
    }

    Column(modifier) {
        HeaderBar(title = "Categories", backArrow = true, onBack = onBack)
        
        LazyColumn(Modifier.fillMaxSize()) {
            item("income_title") {
                RowTitle(title = "Income")
            }
            categoryItems(incomeCategories, incomeDragScope)
            item("expense_title") {
                RowTitle(title = "Expense", modifier = Modifier.padding(top = 20.dp))
            }
            categoryItems(expenseCategories, expenseDragScope)
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
                expenseCategories = previewExpenseCategories,
            )
        }
    }
}