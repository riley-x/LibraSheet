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

    NumberTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = "mm-dd-yy",
        modifier = modifier,
    )
}