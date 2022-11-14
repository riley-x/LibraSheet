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
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.librasheet.ui.components.ColorIndicator
import com.example.librasheet.ui.components.DropdownSelector
import com.example.librasheet.ui.components.libraRowHeight
import com.example.librasheet.ui.dialogs.Dialog
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
        modifier = modifier.fillMaxWidth()
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
    modifier: Modifier = Modifier,
    lines: Int = 1,
    onValueChange: (String) -> Unit = { },
) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }

    TransactionFieldRow(
        label = label,
        alignment = if (lines == 1) Alignment.CenterVertically else Alignment.Top,
        modifier = modifier.height(Dp(maxOf(50f, lines * 25f)))
    ) {
        BasicTextField(
            value = text,
            onValueChange = onValueChange,
            singleLine = lines == 1,
            maxLines = lines,
            textStyle = MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.high)
            ),
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier
                .weight(10f)
                .focusRequester(focusRequester)
                .onFocusChanged { focused = it.isFocused }
        ) {
            Box(
                contentAlignment = if (lines == 1) Alignment.CenterStart else Alignment.TopStart,
                modifier = Modifier.fillMaxSize()
            ) {
                if (focused) {
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

                val color = if (focused) MaterialTheme.colors.primary else Color.Unspecified
                val radius: Float
                val width: Float
                with(LocalDensity.current) {
                    radius = 10.dp.toPx()
                    width = 1.dp.toPx()
                }
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)) {
                    drawRoundRect(
                        color = color,
                        cornerRadius = CornerRadius(radius, radius),
                        style = Stroke(width = width),
                    )
                }
            }
        }
        IconButton(onClick = { focusRequester.requestFocus() }) {
            Icon(imageVector = Icons.Sharp.Edit, contentDescription = null)
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> TransactionSelectorRow(
    label: String,
    selection: T,
    options: List<T>,
    modifier: Modifier = Modifier,
    onSelection: (T) -> Unit = { },
    display: @Composable RowScope.(T) -> Unit = { },
) {
    TransactionFieldRow(
        label = label,
        modifier = modifier.height(libraRowHeight),
    ) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                display(selection)
                Spacer(Modifier.weight(10f))
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach {
                    DropdownMenuItem(
                        onClick = {
                            onSelection(it)
                            expanded = false
                        },
                    ) {
                        display(it)
                    }
                }
            }
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
            )
        }
    }
}


@Preview
@Composable
private fun PreviewSelect() {
    LibraSheetTheme {
        Surface {
            TransactionSelectorRow(
                label = "Account",
                selection = "Robinhood",
                options = listOf(""),
            ) {
                ColorIndicator(Color.Green)
                Text(it)
            }
        }
    }
}