package com.example.librasheet.ui.components.selectors

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.ColorIndicator
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Newer dropdown selector with custom drawables. See below for old text-based version.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> DropdownSelector(
    selection: T,
    options: List<T>,
    modifier: Modifier = Modifier,
    delayOpen: Long = 0,
    onSelection: (T) -> Unit = { },
    display: @Composable RowScope.(T) -> Unit = { },
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun onExpandedChange(new: Boolean) {
        if (delayOpen > 0) scope.launch {
            /**
             * When switching between a text box and a selector, the closing keyboard causes the
             * dropdown box to flicker between above and below. The delay allows the keyboard to
             * close first. TODO only delay when keyboard is open? But isImeVisible is false already
             */
            if (!expanded) delay(delayOpen)
            expanded = !expanded
        } else {
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
             * clicked, but also the ripple is confusing. **/

            /** Using ExposedDropdownMenuDefaults.TrailingIcon doesn't close the keyboard when
             * clicked, but also the ripple is confusing. **/
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




/**
 * Wrapper class for a read-only ExposedDropdownMenu
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> DropdownSelector(
    toString: (T) -> String,
    currentValue: T,
    allValues: List<T>,
    modifier: Modifier = Modifier,
    label: String = "",
    onSelection: (T) -> Unit = { },
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = toString(currentValue),
            onValueChange = {  },
            label = { if (label.isNotBlank()) Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allValues.forEach {
                DropdownMenuItem(
                    onClick = {
                        onSelection(it)
                        expanded = false
                    }
                ) {
                    Text(text = toString(it))
                }
            }
        }
    }
}


@Composable
fun <T: HasDisplayName> DropdownSelector(
    currentValue: T,
    allValues: ImmutableList<T>,
    modifier: Modifier = Modifier,
    label: String = "",
    onSelection: (T) -> Unit = { },
) {
    DropdownSelector(
        toString = { it.displayName },
        currentValue = currentValue,
        allValues = allValues.items,
        modifier = modifier,
        label = label,
        onSelection = onSelection,
    )
}

@Composable
fun DropdownSelector(
    currentValue: String,
    allValues: List<String>,
    modifier: Modifier = Modifier,
    label: String = "",
    onSelection: (String) -> Unit = { },
) {
    DropdownSelector(
        toString = { it },
        currentValue = currentValue,
        allValues = allValues,
        modifier = modifier,
        label = label,
        onSelection = onSelection,
    )
}





@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            DropdownSelector(
                currentValue = "Option 1",
                allValues = emptyList()
            )
        }
    }
}


@Preview
@Composable
private fun PreviewCustom() {
    LibraSheetTheme {
        Surface {
            DropdownSelector(
                selection = "Robinhood",
                options = emptyList(),
            ) {
                ColorIndicator(Color.Green)
                Text(it)
            }
        }
    }
}