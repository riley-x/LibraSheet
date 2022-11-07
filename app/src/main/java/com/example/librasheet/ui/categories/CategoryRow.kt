package com.example.librasheet.ui.categories

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ExpandLess
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Category
import com.example.librasheet.viewModel.preview.previewIncomeCategories

@Composable
fun CategoryRow(
    category: Category,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    subRowModifier: @Composable (Int, Category) -> Modifier = { _, _ -> Modifier },
    subRowContent: @Composable RowScope.(Int, Category) -> Unit = { _, _ -> },
    onExpand: () -> Unit = { },
    content: @Composable RowScope.() -> Unit = { },
) {
    ColorCodedRow(
        color = category.color,
        modifier = modifier,
    ) {
        Text(category.name)
        if (category.subCategories.isNotEmpty()) {
            IconButton(onClick = onExpand) {
                if (expanded) Icon(imageVector = Icons.Sharp.ExpandLess, contentDescription = null)
                else Icon(imageVector = Icons.Sharp.ExpandMore, contentDescription = null)
            }
        }
        content()
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            CategoryRow(
                category = previewIncomeCategories[0],
                expanded = false,
            )
        }
    }
}
