package com.example.librasheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

val libraRowHorizontalPadding = 20.dp
val libraRowHeight = 50.dp

/**
 * Base row composable for various lists, like accounts, income, or expenses.
 */
@Composable
fun ColorCodedRow(
    color: Color,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = libraRowHorizontalPadding,
    content: @Composable (RowScope.() -> Unit) = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(libraRowHeight)
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    ) {
        Spacer(
            Modifier
                .padding(vertical = 8.dp)
                .width(4.dp)
                .fillMaxHeight()
                .background(color = color)
        )
        Spacer(Modifier.width(12.dp))

        content()
    }
}

@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    Divider(
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
        thickness = 1.dp,
        modifier = modifier
            .padding(horizontal = libraRowHorizontalPadding)
    )
}

@Composable
fun RowTitle(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = libraRowHorizontalPadding)
            .heightIn(min = 48.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h2,
            modifier = Modifier.weight(10f)
        )

        content()
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Column {
                RowTitle(title = "Income")
                RowDivider()
                ColorCodedRow(color = Color.Green)
            }
        }
    }
}