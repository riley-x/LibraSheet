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
    initialSelection: String = "",
    title: String = "",
    errorMessage: String = "",
    cancelText: String = "Cancel",
    okText: String = "Ok",
    toString: (String) -> String = { it },
    onDismiss: (String) -> Unit = { },
) {
    var currentSelection by remember { mutableStateOf(initialSelection) }

    Dialog(
        title = title,
        okText = okText,
        cancelText = cancelText,
        errorMessage = errorMessage,
        onCancel = { onDismiss("") },
        onOk = { onDismiss(currentSelection) },
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