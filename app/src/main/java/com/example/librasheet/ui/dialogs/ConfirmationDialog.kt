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
    AlertDialog(
        onDismissRequest = { onDismiss(false) },
        // don't use the title argument, it really messes with the layouts
        text = {
            Text(
                text = text,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        },
        buttons = {
            ConfirmationButtons(
                onCancel = { onDismiss(false) },
                onOk = { onDismiss(true) },
            )
        },
        modifier = modifier,
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