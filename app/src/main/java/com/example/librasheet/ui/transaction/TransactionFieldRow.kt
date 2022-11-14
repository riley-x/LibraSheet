package com.example.librasheet.ui.transaction

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.ColorIndicator
import com.example.librasheet.ui.components.DropdownSelector
import com.example.librasheet.ui.components.OutlinedTextFieldNoPadding
import com.example.librasheet.ui.components.libraRowHeight
import com.example.librasheet.ui.dialogs.Dialog
import com.example.librasheet.ui.theme.LibraSheetTheme
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TransactionFieldRow(
    label: String,
    modifier: Modifier = Modifier,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit = { },
) {
    Row(
        verticalAlignment = alignment,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .then(modifier)
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.body2,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
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
    placeholder: String = "",
    number: Boolean = true,
    error: Boolean = false,
    onValueChange: (String) -> Unit = { },
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    val lines = if (number) 1 else 3


    TransactionFieldRow(
        label = label,
        alignment = if (lines == 1) Alignment.CenterVertically else Alignment.Top,
        modifier = modifier.height(Dp(maxOf(50f, lines * 25f)))
    ) {
        OutlinedTextFieldNoPadding(
            value = text,
            onValueChange = onValueChange,
            isError = error,
            singleLine = lines == 1,
            maxLines = lines,
            contentPadding = PaddingValues(6.dp),
            shape = RoundedCornerShape(10.dp),
            placeholder = {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    Text(
                        text = placeholder,
                    )
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(true) }
            ),
            modifier = Modifier.focusRequester(focusRequester).weight(10f).fillMaxHeight()
        )

        IconButton(onClick = { focusRequester.requestFocus() }) {
            Icon(imageVector = Icons.Sharp.Edit, contentDescription = null)
        }
    }
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
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
        val scope = rememberCoroutineScope()
//        val isImeVisible = WindowInsets.isImeVisible

        fun onExpandedChange(new: Boolean) {
            scope.launch {
                /**
                 * When switching between a text box and a selector, the closing keyboard causes the
                 * dropdown box to flicker between above and below. The delay allows the keyboard to
                 * close first. TODO only delay when keyboard is open? But isImeVisible is false already
                 */
                if (!expanded) delay(100)
                expanded = !expanded
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = ::onExpandedChange,
            modifier = modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                display(selection)
                Spacer(Modifier.weight(10f))

                /** Using ExposedDropdownMenuDefaults.TrailingIcon doesn't close the keyboard when
                 * clicked, but also the ripple is confusing.
                 */
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null)
                }
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
                number = false,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewEditBox() {
    LibraSheetTheme {
        Surface {
            TransactionEditRow(
                label = "Date",
                text = "",
                placeholder = "MM-DD-YY",
                number = true,
                error = true,
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