package com.example.librasheet.ui.categories

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import com.example.librasheet.ui.components.DragToReorderTarget
import com.example.librasheet.ui.components.rowDivider
import com.example.librasheet.viewModel.dataClasses.CategoryUi


/**
 * @param startIndex is only used for determining whether to draw a divider
 */
@Composable
fun CategoryDragRow(
    category: CategoryUi,
    group: String,
    index: Int,
    expanded: SnapshotStateMap<String, MutableTransitionState<Boolean>>,
    modifier: Modifier = Modifier,
    startIndex: Int = 0,
    onReorder: (parentId: String, startIndex: Int, endIndex: Int) -> Unit = { _, _, _ -> },
    content: @Composable RowScope.(CategoryUi) -> Unit = { },
    subContent: @Composable RowScope.(CategoryUi) -> Unit = { },
) {
    /** Note you have to draw the dividers with the composables, because the size of each
     * category row can be different if they're expanded. So it doesn't make sense to have
     * fixed dividers like in the EditAccountScreen. **/
    val dividerColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)

    DragToReorderTarget(
        index = index,
        group = group,
        onDragEnd = onReorder,
        modifier = modifier,
    ) { dragScope ->
        CategoryRow(
            category = category,
            expanded = expanded.getOrPut(category.id.fullName) { MutableTransitionState(false) },
            modifier = Modifier.rowDivider(enabled = index > startIndex && !dragScope.isTarget(group, index), color = dividerColor),
            content = content,
        ) { subIndex, subCategory ->
            CategorySubRow(
                category = subCategory,
                indicatorColor = category.color.copy(alpha = 0.5f),
                last = subIndex == category.subCategories.lastIndex,
                dragIndex = subIndex,
                dragGroup = category.id.fullName,
                onDragEnd = onReorder,
                content = subContent,
            )
        }
    }
}