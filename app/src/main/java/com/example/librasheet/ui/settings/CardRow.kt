package com.example.librasheet.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val cardRowHorizontalPadding = 10.dp

fun Modifier.cardRow() = heightIn(min = 40.dp)
    .padding(horizontal = cardRowHorizontalPadding, vertical = 6.dp)

@Composable
fun ClickableRow(
    name: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .cardRow()
    ) {
        Text(name, modifier = Modifier.weight(10f))
        Icon(Icons.Sharp.ArrowForwardIos, null)
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
