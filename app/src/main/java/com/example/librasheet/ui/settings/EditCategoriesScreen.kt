package com.example.librasheet.ui.settings

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.librasheet.data.database.*
import com.example.librasheet.ui.categories.CategoryDragRow
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.*
import com.example.librasheet.viewModel.preview.*
import com.example.librasheet.viewModel.dataClasses.CategoryUi


private enum class CategoryOption(override val displayName: String) : HasDisplayName {
    RENAME("Rename"),
    COLOR("Change Color"),
    ADD("Add Subcategory"),
    MOVE("Move"),
    DELETE("Delete"),
}

private val categoryOptions = ImmutableList(CategoryOption.values().toList())
private val subCategoryOptions = ImmutableList(categoryOptions.items.filter { it != CategoryOption.ADD })



@Composable
fun EditCategoriesScreen(
    incomeCategories: SnapshotStateList<CategoryUi>,
    expenseCategories: SnapshotStateList<CategoryUi>,
    expanded: SnapshotStateMap<String, MutableTransitionState<Boolean>>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onChangeName: (CategoryUi) -> Unit = { },
    onChangeColor: (String) -> Unit = { },
    onAddCategory: (CategoryId) -> Unit = { },
    onMoveCategory: (CategoryUi) -> Unit = { },
    onDelete: (CategoryUi) -> Unit = { },
    onReorder: (parentId: String, startIndex: Int, endIndex: Int) -> Unit = { _, _, _ -> },
) {
    fun onOptionSelect(category: CategoryUi, categoryOption: CategoryOption) {
        when (categoryOption) {
            CategoryOption.RENAME -> onChangeName(category)
            CategoryOption.COLOR -> onChangeColor("category_" + category.id.fullName)
            CategoryOption.ADD -> onAddCategory(category.id)
            CategoryOption.MOVE -> onMoveCategory(category)
            CategoryOption.DELETE -> onDelete(category)
        }
    }

    Column(modifier) {
        HeaderBar(
            title = "Categories",
            backArrow = true,
            onBack = onBack,
            modifier = Modifier.zIndex(10f)
        )

        DragHost {

            fun LazyListScope.categoryItems(
                list: SnapshotStateList<CategoryUi>,
                group: String,
            ) {
                /** Warning the lazy column must be keyed with the index. Keying the lazy column like
                 * `key = { _, it -> it.id.fullName }` messes up the passed index into DragToReorderTarget,
                 * which won't be reset. **/
                itemsIndexed(list) { index, category ->
                    CategoryDragRow(
                        category = category,
                        group = group,
                        index = index,
                        expanded = expanded,
                        onReorder = onReorder,
                        content = { cat ->
                            Spacer(modifier = Modifier.weight(10f))
                            DropdownOptions(options = categoryOptions) {
                                onOptionSelect(cat, it)
                            }
                        },
                        subContent = { cat ->
                            Spacer(modifier = Modifier.weight(10f))
                            DropdownOptions(options = subCategoryOptions) {
                                onOptionSelect(cat, it)
                            }
                        }
                    )
                }
            }

            fun LazyListScope.categoryTitle(title: String) {
                item(title) {
                    RowTitle(title = title) {
                        IconButton(onClick = { onAddCategory(title.toCategoryId()) }) {
                            Icon(Icons.Sharp.Add, null)
                        }
                        Spacer(Modifier.width(libraRowHorizontalPadding))
                    }
                }
            }

            LazyColumn(Modifier.fillMaxSize()) {
                categoryTitle(incomeName)
                categoryItems(incomeCategories, incomeName)
                item("spacer") { Spacer(Modifier.height(20.dp)) }
                categoryTitle(expenseName)
                categoryItems(expenseCategories, expenseName)
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
            EditCategoriesScreen(
                incomeCategories = previewIncomeCategories,
                expenseCategories = previewExpenseCategories,
                expanded = previewExpanded,
            )
        }
    }
}