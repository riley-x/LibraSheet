package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

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
    cancelText: String = "Cancel",
    okText: String = "Ok",
    onDismiss: (String) -> Unit = { },
) {
    var text by remember { mutableStateOf(initialText) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        awaitFrame() // This is necessary for the keyboard to popup.
        // https://stackoverflow.com/questions/69750447/jetpack-compose-focus-requester-not-working-with-dialog
        focusRequester.requestFocus()
    }

    Dialog(
        title = title,
        okText = okText,
        cancelText = cancelText,
        errorMessage = errorMessage,
        onCancel = { onDismiss("") },
        onOk = { onDismiss(text) },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = text,
            textStyle = MaterialTheme.typography.h5,
            onValueChange = { text = it },
            singleLine = true,
            placeholder = {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(placeholder)
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .height(65.dp) // Note a minimum height is required for the droplet to appear. 60.dp was too small
        )
    }
}

@Preview(
    widthDp = 360
)
@Composable
private fun Preview() {
    LibraSheetTheme {
        TextFieldDialog(
            title = "Add Account",
            placeholder = "Account name",
        )
    }
}

@Preview(
    widthDp = 360
)
@Composable
private fun PreviewError() {
    LibraSheetTheme {
        TextFieldDialog(
            title = "Add Account",
            placeholder = "Account name",
            errorMessage = "Error: account exists already"
        )
    }
}