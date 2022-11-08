package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun ConfirmationDialog(
    text: String,
    modifier: Modifier = Modifier,
    onDismiss: (confirmed: Boolean) -> Unit = { },
) {
    Dialog(
        title = text,
        modifier = modifier,
        onOk = { onDismiss(true) },
        onCancel = { onDismiss(false) },
    )
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        ConfirmationDialog(
            "Confirm me!"
        )
    }
}