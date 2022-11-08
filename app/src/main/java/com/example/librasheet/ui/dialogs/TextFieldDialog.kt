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

    AlertDialog(
        onDismissRequest = { onDismiss("") },
        // don't use the title argument, it really messes with the layouts
        text = {
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colors.onSurface,
                )
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                    ),
                    modifier = Modifier.height(60.dp).padding(vertical = 6.dp)
                )
                Text(
                    text = if (error) errorMessage else "",
                    color = MaterialTheme.colors.error,
                )
            }
        },
        buttons = {
            ConfirmationButtons(
                cancelText = cancelText,
                okText = okText,
                onCancel = { onDismiss("") },
                onOk = { onDismiss(text) },
            )
        },
        modifier = modifier,
    )
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