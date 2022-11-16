package com.example.librasheet.ui.components.textFields

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp


fun Modifier.textFieldBorder() = composed {
    this.border(
            width = 1.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
            shape = MaterialTheme.shapes.small,
        )
        .padding(horizontal = 16.dp, vertical = 4.dp)
}