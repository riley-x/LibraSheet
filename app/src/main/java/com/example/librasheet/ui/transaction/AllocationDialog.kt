package com.example.librasheet.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.Allocation
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.dialogs.DialogHolder
import com.example.librasheet.ui.components.selectors.CategorySelector
import com.example.librasheet.ui.components.textFields.textFieldBorder
import com.example.librasheet.ui.dialogs.Dialog
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.preview.previewCategory2
import com.example.librasheet.viewModel.preview.previewIncomeCategories2

class AllocationDialog(
    private val viewModel: LibraViewModel,
): DialogHolder {
    override var isOpen by mutableStateOf(false)
        private set

    private var isIncome = false
    private var errorMessage by mutableStateOf("")
    private var name = mutableStateOf("")
    private var value = mutableStateOf("")
    private var category = mutableStateOf(Category.None)

    private var onSave: ((String, Long, Category?) -> Unit)? = null

    fun open(
        isIncome: Boolean,
        allocation: Allocation? = null,
        onSave: (String, Long, Category?) -> Unit
    ) {
        isOpen = true
        name.value = allocation?.name ?: ""
        value.value = allocation?.value?.toFloatDollar()?.toString() ?: ""
        category.value = allocation?.category ?: Category.None
        this.isIncome = isIncome
        this.onSave = onSave
    }

    fun clear() {
        isOpen = false
        errorMessage = ""
    }

    fun onClose(cancelled: Boolean) {
        if (cancelled) { clear() }
        else {
            val valueLong = value.value.toDoubleOrNull()?.toLongDollar()
            if (valueLong == null) {
                errorMessage = "Couldn't parse value"
            } else if (valueLong < 0) {
                errorMessage = "Value should be positive"
            } else {
                onSave?.invoke(name.value, valueLong, category.value)
                clear()
            }
        }
    }

    @Composable
    override fun Content() {
        if (isOpen) {
            AllocationDialogComposable(
                name = name,
                value = value,
                category = category,
                categories = if (isIncome) viewModel.categories.incomeTargets else viewModel.categories.expenseTargets,
                onClose = ::onClose,
            )
        }
    }
}


@Composable
private fun AllocationDialogComposable(
    name: MutableState<String>,
    value: MutableState<String>,
    category: MutableState<Category>,
    categories: List<Category>,
    onClose: (cancelled: Boolean) -> Unit = { },
) {
    Dialog(
        onCancel = { onClose(true) },
        onOk =  { onClose(false) },
    ) {
        OutlinedTextField(
            value = name.value,
            label = { Text("Name") },
            onValueChange = { name.value = it },
            singleLine = true,
            placeholder = {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text("name")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 30.dp)
                .height(65.dp)
        )


        OutlinedTextField(
            value = value.value,
            label = { Text("Value") },
            onValueChange = { value.value = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 30.dp)
                .height(65.dp)
        )


        CategorySelector(
            selection = category.value,
            options = categories,
            onSelection = { category.value = it ?: Category.None },
            modifier = Modifier.padding(bottom = 6.dp).textFieldBorder(label = "Category")
        )
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        AllocationDialogComposable(
            name = remember { mutableStateOf("This is a name") },
            value = remember { mutableStateOf("356.42") },
            category = previewCategory2,
            categories = previewIncomeCategories2,
        )
    }
}
