package com.example.librasheet.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.*
import kotlin.math.exp
import kotlin.math.roundToInt


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
    var currentDragId by remember { mutableStateOf(-1) }
    var dragOffset by remember { mutableStateOf(0f) }

    @Composable
    fun Modifier.drag(category: Category): Modifier {
        val haptic = LocalHapticFeedback.current
        var out = this
        if (category.id == currentDragId) out = out.offset { IntOffset(0, dragOffset.roundToInt()) }
        return out.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentDragId = category.id
                    dragOffset = 0f
                },
                onDragEnd = {
                    currentDragId = -1
                },
                onDragCancel = {
                    currentDragId = -1
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount.y
                }
            )
        }
    }



    fun LazyListScope.categoryItems(list: SnapshotStateList<Category>) {
        itemsIndexed(list) { index, category ->
            if (index > 0) RowDivider()

            CategoryRow(
                category = category,
                modifier = Modifier.drag(category),
                subRowModifier = { Modifier.drag(it) },
                subRowContent = { subCategory ->
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
                expenseCategories = previewExpenseCategories,
            )
        }
    }
}