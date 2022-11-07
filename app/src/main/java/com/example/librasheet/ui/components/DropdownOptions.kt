package com.example.librasheet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList

@Composable
fun <T: HasDisplayName> DropdownOptions(
    options: ImmutableList<T>,
    modifier: Modifier = Modifier,
    onSelection: (T) -> Unit = { },
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.items.forEach {
                DropdownMenuItem(onClick = {
                    onSelection(it)
                    expanded = false
                }) {
                    Text(it.displayName)
                }
            }
        }
    }
}
