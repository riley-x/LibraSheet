package com.example.librasheet.ui.settings.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.Institution
import com.example.librasheet.data.allInstitutions
import com.example.librasheet.data.entity.Category
import com.example.librasheet.ui.components.DialogHolder
import com.example.librasheet.ui.components.selectors.CategorySelector
import com.example.librasheet.ui.components.selectors.DropdownSelector
import com.example.librasheet.ui.components.textFields.textFieldBorder
import com.example.librasheet.ui.dialogs.Dialog
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.preview.previewCategory2
import com.example.librasheet.viewModel.preview.previewIncomeCategories2

class AccountDialog(
    private val viewModel: LibraViewModel,
): DialogHolder {
    override var isOpen by mutableStateOf(false)
        private set

    private var name = mutableStateOf("")
    private var institution = mutableStateOf(Institution.UNKNOWN)
    private var onSave: (() -> Unit)? = null

    fun openNew() {
        isOpen = true
        name.value = ""
        institution.value = Institution.UNKNOWN
        onSave = { viewModel.accounts.add(name.value, institution.value) }
    }

    fun openEdit(
        index: Int,
    ) {
        isOpen = true
        val current = viewModel.accounts.all[index]
        this.name.value = current.name
        this.institution.value = current.institution
        this.onSave = { viewModel.accounts.update(index, name.value, institution.value) }
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
            AccountDialogComposable(
                name = name,
                institution = institution,
                onCancel = ::onCancel,
                onOk = ::onOk,
            )
        }
    }
}


@Composable
private fun AccountDialogComposable(
    name: MutableState<String>,
    institution: MutableState<Institution>,
    onCancel: () -> Unit = { },
    onOk: () -> Unit = { },
) {
    Dialog(
        onCancel = onCancel,
        onOk = onOk,
    ) {
        OutlinedTextField(
            value = name.value,
            label = { Text("Pattern") },
            onValueChange = { name.value = it },
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

        DropdownSelector(
            currentValue = institution.value,
            allValues = allInstitutions,
            label = "Institution",
            onSelection = { institution.value = it },
        )
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        AccountDialogComposable(
            name = remember { mutableStateOf("Robinhood") },
            institution = remember { mutableStateOf(Institution.BANK_OF_AMERICA) },
        )
    }
}