package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.database.CategoryId
import com.example.librasheet.data.database.toCategoryId
import com.example.librasheet.ui.components.DropdownSelector
import com.example.librasheet.ui.dialogs.Dialog
import com.example.librasheet.ui.dialogs.SelectorDialog
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.CategoryUi
import com.example.librasheet.viewModel.preview.previewIncomeCategories

@Composable
fun CategoryRuleDialog(
    currentPattern: String,
    currentCategory: CategoryUi,
    categories: SnapshotStateList<CategoryUi>,
    onClose: (cancelled: Boolean, String, CategoryUi) -> Unit = { _, _, _ -> },
) {
    var pattern by remember { mutableStateOf(currentPattern) }
    var category by remember { mutableStateOf(currentCategory) }

    Dialog(
        onCancel = { onClose(true, pattern, category) },
        onOk =  { onClose(false, pattern, category) },
    ) {
        OutlinedTextField(
            value = pattern,
            label = { Text("Pattern") },
            onValueChange = { pattern = it },
            singleLine = true,
            placeholder = {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text("pattern")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 30.dp)
                .height(60.dp)
        )

        DropdownSelector(
            label = "Category",
            toString = { it.id.name },
            currentValue = category,
            allValues = categories,
            onSelection = { category = it },
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        CategoryRuleDialog(
            currentPattern = "PYPAL",
            currentCategory = previewIncomeCategories[0],
            categories = previewIncomeCategories,
        )
    }
}
