package com.example.librasheet.ui.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ExpandLess
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.TransactionCategory
import com.example.librasheet.viewModel.preview.previewIncomeCategories

@Composable
fun CategoryRow(
    category: TransactionCategory,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable(category) { mutableStateOf(false) }

    Column(modifier = modifier) {
        ColorCodedRow(color = category.color) {
            Text(category.name)
            if (category.subCategories.isNotEmpty()) {
                IconButton(onClick = { expanded = !expanded }) {
                    if (expanded) Icon(imageVector = Icons.Sharp.ExpandLess, contentDescription = null)
                    else Icon(imageVector = Icons.Sharp.ExpandMore, contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.weight(10f))
            Text(formatDollar(category.value))
        }

        if (category.subCategories.isNotEmpty()) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
                exit = shrinkVertically() + fadeOut()
            ) {
                (category.subCategories).forEachIndexed { index, cat ->
                    CategorySubRow(
                        category = cat,
                        last = index == category.subCategories.lastIndex,
                        modifier = Modifier
//                            .clickable { onPositionClick(pos) }
//                            .padding(horizontal = horizontalPadding)
                        // padding needs to be here so that the clickable animation covers the full width
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            CategoryRow(
                category = previewIncomeCategories[0]
            )
        }
    }
}