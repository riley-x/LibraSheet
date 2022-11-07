package com.example.librasheet.ui.categories

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.librasheet.ui.cashFlow.CashFlowScreen
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.*
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sign


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



@OptIn(ExperimentalFoundationApi::class)
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
    val rowHeight = with(LocalDensity.current) { (libraRowHeight + 1.dp).toPx().roundToInt() }
    val haptic = LocalHapticFeedback.current
    var currentDragIndex by remember { mutableStateOf(-1) }
    var currentParentId by remember { mutableStateOf(-3) } // use -1 for income, -2 for expense
    var dragOffset by remember { mutableStateOf(0f) }
    val currentHoverIndex by remember { derivedStateOf { currentDragIndex + (dragOffset / rowHeight).toInt() } }

    @Composable
    fun Modifier.drag(index: Int, parentId: Int): Modifier {
        val zIndex = if (parentId == currentParentId && index == currentDragIndex) 10f else 0f
        Log.d("Libra", "$parentId $index $zIndex")

        return this
            .offset {
                IntOffset(
                    0,
                    if (parentId != currentParentId) 0
                    else if (index == currentDragIndex) dragOffset.roundToInt()
                    else if (currentHoverIndex > currentDragIndex
                        && index > currentDragIndex
                        && index <= currentHoverIndex
                    ) -rowHeight
                    else if (currentHoverIndex < currentDragIndex
                        && index < currentDragIndex
                        && index >= currentHoverIndex
                    ) rowHeight
                    else 0
                )
            }
            .background(MaterialTheme.colors.surface)
            .zIndex(zIndex) // THIS NEEDS TO BE APPLIES TO THE COLUMN!!!!
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentDragIndex = index
                        currentParentId = parentId
                        dragOffset = 0f
                    },
                    onDragEnd = {
                        currentDragIndex = -1
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        currentDragIndex = -1
                        dragOffset = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount.y
                    }
                )
            }
    }

    val incomeExpanded = remember(incomeCategories) { incomeCategories.map { false }.toMutableStateList() }
    val expenseExpanded = remember(expenseCategories) { expenseCategories.map { false }.toMutableStateList() }

    fun LazyListScope.categoryItems(list: SnapshotStateList<Category>, expanded: SnapshotStateList<Boolean>, id: Int) {
        list.forEachIndexed { index, category ->
            item(category.id) {
                if (index > 0) RowDivider()

                CategoryRow(
                    category = category,
                    expanded = expanded[index],
                    onExpand = { expanded[index] = !expanded[index] },
                    modifier = Modifier.drag(index, id),
//                    subRowModifier = { subIndex, _ -> Modifier.drag(subIndex, category.id) },
//                    subRowContent = { _, subCategory ->
//                        Spacer(modifier = Modifier.weight(10f))
//                        DropdownOptions(options = subCategoryOptions) {
//                            when (it) {
//                                SubCategoryOptions.RENAME -> onChangeName(subCategory)
//                                SubCategoryOptions.COLOR -> onChangeColor("category_${subCategory.name}")
//                                SubCategoryOptions.MOVE -> onMoveSubCategory(subCategory)
//                                SubCategoryOptions.DELETE -> onDelete(subCategory)
//                            }
//                        }
//                    },
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

            if (category.subCategories.isNotEmpty() && expanded[index]) {
                itemsIndexed(category.subCategories, key = { _, it -> it.id }) { subIndex, subCat ->
                    CategorySubRow(
                        category = subCat,
                        indicatorColor = category.color.copy(alpha = 0.5f),
                        last = subIndex == category.subCategories.lastIndex,
                        modifier = Modifier.animateItemPlacement()
                    ) {
//                                subRowContent(subIndex, cat)
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
            categoryItems(incomeCategories, incomeExpanded, -1)
            item("expense_title") {
                RowTitle(title = "Expense", modifier = Modifier.padding(top = 20.dp))
            }
            categoryItems(expenseCategories, expenseExpanded, -2)
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