package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CardRowDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
    thickness: Dp = 1.dp,
) {
    Divider(
        color = color,
        thickness = thickness,
        modifier = modifier
            .padding(horizontal = 4.dp)
    )
}