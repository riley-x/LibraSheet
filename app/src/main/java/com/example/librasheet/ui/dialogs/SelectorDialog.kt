package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.DropdownSelector
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun SelectorDialog(
    options: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    initialSelection: String = options[0],
    title: String = "",
    errorMessage: String = "",
    error: Boolean = false,
    cancelText: String = "Cancel",
    okText: String = "Ok",
    onDismiss: (String) -> Unit = { },
) {
    var currentSelection by remember { mutableStateOf(initialSelection) }

    AlertDialog(
        onDismissRequest = { onDismiss("") },
        // don't use the title argument, it really messes with the layouts
        text = {
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colors.onSurface,
                )
                DropdownSelector(
                    currentValue = currentSelection,
                    allValues = options,
                    onSelection = { currentSelection = it },
                    modifier = Modifier.padding(bottom = 6.dp)
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
                onOk = { onDismiss(currentSelection) },
            )
        },
        modifier = modifier,
    )
}


@Preview
@Composable
private fun Preview() {
    val options = remember {
        mutableStateListOf("Option 1")
    }
    LibraSheetTheme {
        SelectorDialog(
            options = options,
            title = "Move To",
        )
    }
}

@Preview
@Composable
private fun PreviewError() {
    val options = remember {
        mutableStateListOf("Option 1")
    }
    LibraSheetTheme {
        SelectorDialog(
            options = options,
            title = "Move To",
            error = true,
            errorMessage = "Error: category exists in selection already"
        )
    }
}