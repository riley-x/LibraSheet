package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.librasheet.data.database.Category
import com.example.librasheet.data.database.CategoryRule
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewIncomeCategories
import com.example.librasheet.viewModel.preview.previewRules


private enum class RuleOptions(override val displayName: String): HasDisplayName {
    EDIT("Edit"),
    DELETE("Delete"),
}
private val ruleOptions = ImmutableList(RuleOptions.values().toList())


@Composable
fun CategoryRulesScreen(
    rules: SnapshotStateList<CategoryRule>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onAdd: () -> Unit = { },
    onEdit: (CategoryRule) -> Unit = { },
    onDelete: (CategoryRule) -> Unit = { },
    onReorder: (startIndex: Int, endIndex: Int) -> Unit = { _, _ -> },
) {
    Column(modifier) {
        HeaderBar(
            title = "Rules",
            backArrow = true,
            onBack = onBack,
            modifier = Modifier.zIndex(2f)
        ) {
            Spacer(Modifier.weight(10f))
            IconButton(onClick = onAdd) {
                Icon(Icons.Sharp.Add, null)
            }
        }

        DragHost {
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(rules) { index, rule ->
                    if (index > 0) RowDivider(Modifier.zIndex(1f))

                    DragToReorderTarget(
                        index = index,
                        onDragEnd = { _, start, end -> onReorder(start, end) },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.libraRow()
                        ) {
                            Text(rule.pattern, modifier = Modifier.weight(10f))
                            Spacer(Modifier.width(12.dp))
                            ColorIndicator(rule.category?.color ?: Color.Unspecified)
                            Text(
                                text = rule.category?.id?.name ?: "None",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier.weight(8f)
                            )
                            DropdownOptions(options = ruleOptions) {
                                when (it) {
                                    RuleOptions.EDIT -> onEdit(rule)
                                    RuleOptions.DELETE -> onDelete(rule)
                                }
                            }
                        }
                    }
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
            CategoryRulesScreen(
                rules = previewRules,
            )
        }
    }
}