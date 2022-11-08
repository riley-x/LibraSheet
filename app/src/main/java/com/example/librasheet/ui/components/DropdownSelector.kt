package com.example.librasheet.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList

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