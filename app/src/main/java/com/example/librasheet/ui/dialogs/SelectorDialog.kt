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
fun <T> SelectorDialog(
    options: SnapshotStateList<T>,
    initialSelection: T,
    toString: (T) -> String,
    modifier: Modifier = Modifier,
    title: String = "",
    errorMessage: String = "",
    cancelText: String = "Cancel",
    okText: String = "Ok",
    onDismiss: (cancelled: Boolean, T) -> Unit = { _, _ -> },
) {
    var currentSelection by remember { mutableStateOf(initialSelection) }

    Dialog(
        title = title,
        okText = okText,
        cancelText = cancelText,
        errorMessage = errorMessage,
        onCancel = { onDismiss(true, currentSelection) },
        onOk = { onDismiss(false, currentSelection) },
        modifier = modifier,
    ) {
        DropdownSelector(
            toString = toString,
            currentValue = currentSelection,
            allValues = options,
            onSelection = { currentSelection = it },
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}


@Composable
fun SelectorDialog(
    options: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    initialSelection: String = "",
    title: String = "",
    errorMessage: String = "",
    cancelText: String = "Cancel",
    okText: String = "Ok",
    toString: (String) -> String = { it },
    onDismiss: (String) -> Unit = { },
) = SelectorDialog(
    options = options,
    modifier = modifier,
    initialSelection = initialSelection,
    title = title,
    errorMessage = errorMessage,
    cancelText = cancelText,
    okText = okText,
    toString = toString,
    onDismiss = { cancelled, text -> if (cancelled) onDismiss("") else onDismiss(text) },
)

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
            errorMessage = "Error: category exists in selection already",
        )
    }
}