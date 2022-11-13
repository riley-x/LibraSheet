package com.example.librasheet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun NoDataError(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Text("No Data",
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.error.copy(alpha = ContentAlpha.medium),
        )
    }
}