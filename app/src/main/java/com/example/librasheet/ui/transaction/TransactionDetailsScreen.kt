package com.example.librasheet.ui.transaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FilterAlt
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.ui.components.DropdownOptions
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowTitle
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.previewTransactions


@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TransactionDetailScreen(
    transaction: TransactionEntity,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit = { },
) {
    /** Keyboard and focus **/
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val name = remember { mutableStateOf(transaction.name) }
    val date = remember { mutableStateOf(transaction.date.toString()) }
    val value = remember { mutableStateOf(transaction.value.toString()) }

    val nameEnabled = remember { mutableStateOf(false) }
    val dateEnabled = remember { mutableStateOf(false) }
    val valueEnabled = remember { mutableStateOf(false) }


    fun clearFocus() {
        nameEnabled.value = false
        dateEnabled.value = false
        valueEnabled.value = false
        keyboardController?.hide()
        focusManager.clearFocus(true)
    }

    fun LazyListScope.editor(
        label: String,
        text: MutableState<String>,
        enabled: MutableState<Boolean>,
        lines: Int = 1,
    ) {
        item(label) {
            TransactionEditRow(
                label = label,
                text = text.value,
                lines = lines,
                enabled = enabled.value,
                onEnable = {
                    clearFocus()
                    enabled.value = true
                    it.requestFocus()
                },
                onValueChange = { text.value = it },
            )
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            /** These paddings need to be placed after the scroll modifier or else they will cause
             * a flicker. The only problem with this is that the bottom ripple doesn't appear anymore.
             */
            .windowInsetsPadding(WindowInsets.ime)
            .padding(bottom = if (WindowInsets.isImeVisible) 0.dp else bottomPadding)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { clearFocus() }
                )
            }
    ) {
        HeaderBar(
            title = "Transactions",
            backArrow = true,
            onBack = onBack,
        )

        LazyColumn {
            item("details") {
                RowTitle("Details")
            }

            editor("Name", name, nameEnabled, lines = 3)
            editor("Date", date, dateEnabled)
            editor("Value", value, valueEnabled)
        }
    }
}




@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionDetailScreen(
                transaction = previewTransactions[0]
            )
        }
    }
}