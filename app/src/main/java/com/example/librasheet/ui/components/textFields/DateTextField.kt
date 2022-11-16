package com.example.librasheet.ui.components.textFields

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun DateTextField(
    value: String,
    modifier: Modifier = Modifier,
    label: String = "",
    onValueChange: (String) -> Unit = { },
) {
    // TODO auto insert the "-"
    // annoying because the cursor might not be at the end. Also need to adjust the cursor
    // position after adding characters.
//            {
//                date.value =
//                    if (date.value.length == 1 && it.length == 2) "$it-"
//                    else if (date.value.length == 4 && it.length == 5) "$it-"
//                    else it
//            }

    NumberTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = "mm-dd-yy",
        modifier = modifier,
    )
}