package com.example.librasheet.ui.transaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.DialogHolder
import com.example.librasheet.ui.dialogs.TextFieldDialog
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.TransactionModel

class ReimbursementDialog: DialogHolder {
    override var isOpen by mutableStateOf(false)
    private var errorMessage by mutableStateOf("")

    private var onSave: ((Long) -> Unit)? = null

    fun open(onSave: (Long) -> Unit) {
        isOpen = true
        this.onSave = onSave
    }

    fun cancel() {
        isOpen = false
        onSave = null
    }

    fun save(input: String) {
        val valueLong = input.toFloatOrNull()?.toLongDollar()
        if (valueLong == null) {
            errorMessage = "Couldn't parse value"
        } else {
            onSave?.invoke(valueLong)
        }
    }

    @Composable
    override fun Content() {
        if (isOpen) {
            TextFieldDialog(
                title = "New Value",
                errorMessage = errorMessage,
                onDismiss = ::save,
            )
        }
    }
}