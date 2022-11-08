package com.example.librasheet.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.*


private enum class CategoryOptions(override val displayName: String) : HasDisplayName {
    RENAME("Rename"),
    COLOR("Change Color"),
    ADD("Add Subcategory"),
    DELETE("Delete"),
}

private enum class SubCategoryOptions(override val displayName: String) : HasDisplayName {
    RENAME("Rename"),
    COLOR("Change Color"),
    MOVE("Change Parent"),
    DELETE("Delete"),
}

private val categoryOptions = ImmutableList(CategoryOptions.values().toList())
private val subCategoryOptions = ImmutableList(SubCategoryOptions.values().toList())

/** Used for DragToReorder to know which group the selected element should be allowed to reorder in.
 * MAKE SURE THESE DON'T CONFLICT WITH OTHER GROUP IDS (like Category.id) **/
const val incomeGroupId = -10
const val expenseGroupId = -20

@Composable
fun CategoriesScreen(
    incomeCategories: SnapshotStateList<Category>,
    expenseCategories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onChangeName: (Category) -> Unit = { },
    onChangeColor: (String) -> Unit = { },
    onAddSubCategory: (Category) -> Unit = { },
    onMoveSubCategory: (Category) -> Unit = { },
    onDelete: (Category) -> Unit = { },
    onDragEnd: (groupId: Int, startIndex: Int, endIndex: Int) -> Unit = { _, _, _ -> },
) {
    Column(modifier) {
        HeaderBar(title = "Categories", backArrow = true, onBack = onBack, modifier = Modifier.zIndex(10f))

        DragHost {
            val dividerColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)
            fun LazyListScope.categoryItems(list: SnapshotStateList<Category>, groupId: Int) {
                itemsIndexed(list) { index, category ->
                    var expanded by remember { mutableStateOf(false) }
                    DragToReorderTarget(
                        index = index,
                        groupId = groupId,
                        contentState = expanded,
                        onDragEnd = { _, _, _ -> },
                    ) { dragScope, _ ->
                        CategoryRow(
                            category = category,
                            expanded = expanded,
                            onExpand = { expanded = !expanded },
                            modifier = Modifier.rowDivider(enabled = index > 0 && !dragScope.isTarget(groupId, index), color = dividerColor),
                            content = { category ->
                                Spacer(modifier = Modifier.weight(10f))
                                DropdownOptions(options = categoryOptions) {
                                    when (it) {
                                        CategoryOptions.RENAME -> onChangeName(category)
                                        CategoryOptions.COLOR -> onChangeColor("category_${category.name}")
                                        CategoryOptions.ADD -> onAddSubCategory(category)
                                        CategoryOptions.DELETE -> onDelete(category)
                                    }
                                }
                            },
                        ) { subIndex, subCategory ->
                            CategorySubRow(
                                category = subCategory,
                                indicatorColor = category.color.copy(alpha = 0.5f),
                                last = subIndex == category.subCategories.lastIndex,
                                dragIndex = subIndex,
                                dragGroup = category.id,
                                onDragEnd = { _, _, _ -> },
                            ) {
                                Spacer(modifier = Modifier.weight(10f))
                                DropdownOptions(options = subCategoryOptions) {
                                    when (it) {
                                        SubCategoryOptions.RENAME -> onChangeName(subCategory)
                                        SubCategoryOptions.COLOR -> onChangeColor("category_${subCategory.name}")
                                        SubCategoryOptions.MOVE -> onMoveSubCategory(subCategory)
                                        SubCategoryOptions.DELETE -> onDelete(subCategory)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(Modifier.fillMaxSize()) {
                item("income_title") {
                    RowTitle(title = "Income")
                }
                categoryItems(incomeCategories, incomeGroupId)
                item("expense_title") {
                    RowTitle(title = "Expense", modifier = Modifier.padding(top = 20.dp))
                }
                categoryItems(expenseCategories, expenseGroupId)
            }
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