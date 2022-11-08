package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

/**
 * Dialog box with a single text field, with ok and cancel buttons
 */
@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    initialText: String = "",
    title: String = "",
    placeholder: String = "",
    errorMessage: String = "",
    error: Boolean = false,
    cancelText: String = "Cancel",
    okText: String = "Ok",
    onDismiss: (String) -> Unit = { },
) {
    var text by remember { mutableStateOf(initialText) }

    Dialog(
        title = title,
        okText = okText,
        cancelText = cancelText,
        error = error,
        errorMessage = errorMessage,
        onCancel = { onDismiss("") },
        onOk = { onDismiss(text) },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = text,
            textStyle = MaterialTheme.typography.h5,
            onValueChange = { text = it },
            placeholder = {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(placeholder)
                }
            },
            modifier = Modifier
                .padding(vertical = 6.dp)
                .height(60.dp)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        TextFieldDialog(
            title = "Add Account",
            placeholder = "Account name",
        )
    }
}

@Preview
@Composable
private fun PreviewError() {
    LibraSheetTheme {
        TextFieldDialog(
            title = "Add Account",
            placeholder = "Account name",
            error = true,
            errorMessage = "Error: account exists already"
        )
    }
}