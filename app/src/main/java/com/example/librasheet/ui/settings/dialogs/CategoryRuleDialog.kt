package com.example.librasheet.ui.settings.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.Category
import com.example.librasheet.ui.components.DialogHolder
import com.example.librasheet.ui.components.selectors.CategorySelector
import com.example.librasheet.ui.components.textFields.textFieldBorder
import com.example.librasheet.ui.dialogs.Dialog
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.preview.previewCategory2
import com.example.librasheet.viewModel.preview.previewIncomeCategories2


class CategoryRuleDialog(
    private val viewModel: LibraViewModel,
): DialogHolder {
    override var isOpen by mutableStateOf(false)
        private set

    private var pattern = mutableStateOf("")
    private var category = mutableStateOf(Category.None)
    private var onSave: (() -> Unit)? = null

    fun openForNewRule() {
        isOpen = true
        onSave = { viewModel.rules.add(pattern.value, category.value) }
    }

    fun openForEditRule(
        index: Int,
    ) {
        isOpen = true
        val current = viewModel.rules.displayList[index]
        this.pattern.value = current.pattern
        this.category.value = current.category ?: Category.None
        this.onSave = { viewModel.rules.update(index, pattern.value, category.value) }
    }

    private fun close() {
        isOpen = false
    }

    private fun onCancel() {
        close()
    }

    private fun onOk() {
        onSave?.invoke()
        close()
    }

    @Composable
    override fun Content() {
        if (isOpen) {
            CategoryRuleDialogComposable(
                pattern = pattern,
                category = category,
                categories = if (viewModel.rules.currentScreenIsIncome)
                    viewModel.categories.incomeTargets else
                    viewModel.categories.expenseTargets,
                onCancel = ::onCancel,
                onOk = ::onOk,
            )
        }
    }
}


@Composable
private fun CategoryRuleDialogComposable(
    pattern: MutableState<String>,
    category: MutableState<Category>,
    categories: List<Category>,
    onCancel: () -> Unit = { },
    onOk: () -> Unit = { },
) {
    Dialog(
        onCancel = onCancel,
        onOk = onOk,
    ) {
        OutlinedTextField(
            value = pattern.value,
            label = { Text("Pattern") },
            onValueChange = { pattern.value = it },
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
                .height(65.dp)
        )

        CategorySelector(
            selection = category.value,
            options = categories,
            onSelection = { category.value = it ?: Category.None },
            modifier = Modifier
                .padding(bottom = 6.dp)
                .textFieldBorder(label = "Category")
        )
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        CategoryRuleDialogComposable(
            pattern = remember { mutableStateOf("PYPAL") },
            category = previewCategory2,
            categories = previewIncomeCategories2,
        )
    }
}
