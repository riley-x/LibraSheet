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
    title: String = "",
    placeholder: String = "",
    onDismiss: (String) -> Unit = { },
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss("") },
        // don't use the title argument, it really messes with the layouts
        text = {
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp),
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
                    modifier = Modifier.height(60.dp)
                )
            }
        },
        buttons = {
            ConfirmationButtons(
                onCancel = { onDismiss("") },
                onOk = { onDismiss(text) },
            )
        },
        modifier = modifier,
    )
}

@Preview
@Composable
fun PreviewTextFieldDialog() {
    LibraSheetTheme {
        TextFieldDialog(
            title = "Add Account",
            placeholder = "Account name",
        )
    }
}