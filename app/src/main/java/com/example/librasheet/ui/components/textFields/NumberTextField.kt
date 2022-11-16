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
fun NumberTextField(
    value: String,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit = { },
) {
    val focusManager = LocalFocusManager.current

    val labelComposable: @Composable (() -> Unit)? =
        if (label.isEmpty()) null
        else {
            { Text(text = label) }
        }

    val placeholderComposable: @Composable (() -> Unit)? =
        if (placeholder.isEmpty()) null
        else {
            { Text(text = placeholder) }
        }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = labelComposable,
        placeholder = placeholderComposable,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus(true) }
        ),
        modifier = modifier,
    )
}