package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.DropdownSelector
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun Dialog(
    modifier: Modifier = Modifier,
    title: String = "",
    errorMessage: String = "",
    cancelText: String = "Cancel",
    okText: String = "Ok",
    onCancel: () -> Unit = { },
    onOk: () -> Unit = { },
    content: @Composable ColumnScope.() -> Unit = { },
) {
    AlertDialog(
        onDismissRequest = onCancel,
        // don't use the title argument, it really messes with the layouts
        text = {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                LocalTextStyle provides MaterialTheme.typography.body1,
            ) {
                Column {
                    Text(text = title)
                    content()
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colors.error,
                        )
                    }
                }
            }
        },
        buttons = {
            ConfirmationButtons(
                cancelText = cancelText,
                okText = okText,
                onCancel = onCancel,
                onOk = onOk,
            )
        },
        modifier = modifier,
    )
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Dialog(
            title = "I'm a simple dialog",
        )
    }
}

@Preview
@Composable
private fun PreviewError() {
    LibraSheetTheme {
        Dialog(
            title = "I'm a simple dialog",
            errorMessage = "Error: Oh no!"
        )
    }
}