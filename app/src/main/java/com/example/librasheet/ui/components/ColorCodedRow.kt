package com.example.librasheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

val LibraRowHeight = 50.dp

/**
 * Base row composable for various lists, like accounts, income, or expenses
 */
@Composable
fun ColorCodedRow(
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit) = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(LibraRowHeight)
    ) {
        Spacer(
            Modifier
                .padding(vertical = 8.dp)
                .width(4.dp)
                .fillMaxHeight()
                .background(color = color)
        )

        content()
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            ColorCodedRow(color = Color.Green)
        }
    }
}