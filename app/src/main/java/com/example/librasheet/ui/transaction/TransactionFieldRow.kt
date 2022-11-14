package com.example.librasheet.ui.transaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun TransactionFieldRow(
    label: String,
    modifier: Modifier = Modifier,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit = { },
) {
    Row(
        verticalAlignment = alignment,
        modifier = modifier
            .fillMaxWidth()
            .requiredHeightIn(min = libraRowHeight)
    ) {
        Text(
            text = label,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.body2,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
            modifier = Modifier
                .padding(top = 6.dp, bottom = 6.dp)
                .padding(end = 15.dp)
                .width(80.dp)
        )
        content()
    }
}


@Composable
fun TransactionEditRow(
    label: String,
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    lines: Int = 1,
    onEnable: (FocusRequester) -> Unit = { },
    onValueChange: (String) -> Unit = { },
) {
    val focusRequester = remember { FocusRequester() }

    TransactionFieldRow(
        label = label,
        alignment = if (lines == 1) Alignment.CenterVertically else Alignment.Top,
        modifier = modifier.height(Dp(maxOf(50f, lines * 25f)))
    ) {
        BasicTextField(
            value = text,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = lines == 1,
            maxLines = lines,
            textStyle = MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.high)
            ),
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier
                .weight(10f)
                .focusRequester(focusRequester)
        ) {
            Box(
                contentAlignment = if (lines == 1) Alignment.CenterStart else Alignment.TopStart,
                modifier = Modifier.fillMaxSize()
            ) {
                if (enabled) {
                    Box(
                        Modifier.padding(6.dp)
                    ) {
                        it()
                    }
                }
                else { // Need this since BasicTextField doesn't have TextOverflow
                    Text(
                        text = text,
                        maxLines = lines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                val color = if (enabled) MaterialTheme.colors.primary else Color.Unspecified
                val radius: Float
                val width: Float
                with(LocalDensity.current) {
                    radius = 10.dp.toPx()
                    width = 1.dp.toPx()
                }
                Canvas(modifier = Modifier.fillMaxSize().padding(1.dp)) {
                    drawRoundRect(
                        color = color,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = width),
                    )
                }
            }
        }
        IconButton(onClick = { onEnable(focusRequester) }) {
            Icon(imageVector = Icons.Sharp.Edit, contentDescription = null)
        }
    }
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionFieldRow(
                label = "Category"
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEdit() {
    LibraSheetTheme {
        Surface {
            TransactionEditRow(
                label = "Name",
                text = "BANK OF AMERICA CREDIT CARD Bill Payment",
                lines = 3,
                enabled = true,
            )
        }
    }
}