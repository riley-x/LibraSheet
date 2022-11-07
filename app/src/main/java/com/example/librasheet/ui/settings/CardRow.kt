package com.example.librasheet.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

val cardRowHorizontalPadding = 10.dp

fun Modifier.cardRow() = heightIn(min = 40.dp)
    .padding(horizontal = cardRowHorizontalPadding, vertical = 6.dp)

@Composable
fun ClickableRow(
    name: String,
    onClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .cardRow()
    ) {
        Text(name, modifier = Modifier.weight(10f))
        Icon(
            imageVector = Icons.Sharp.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
        )
    }
}


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


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Column {
                ClickableRow(name = "Edit")
                CardRowDivider()
                ClickableRow(name = "Rules")
            }
        }
    }
}
