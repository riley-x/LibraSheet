package com.example.librasheet.ui.transaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    var name by remember { mutableStateOf(transaction.name) }
    var date by remember { mutableStateOf(transaction.date.toString()) }

    var nameEnabled by remember { mutableStateOf(false) }
    var dateEnabled by remember { mutableStateOf(false) }


    fun clearFocus() {
        nameEnabled = false
        dateEnabled = false
        keyboardController?.hide()
        focusManager.clearFocus(true)
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

            item("name") {
                TransactionEditRow(
                    label = "Name",
                    text = name,
                    lines = 3,
                    enabled = nameEnabled,
                    onEnable = { clearFocus(); nameEnabled = true },
                    onValueChange = { name = it },
                )
            }

            item("date") {
                TransactionEditRow(
                    label = "Date",
                    text = date,
                    enabled = dateEnabled,
                    onEnable = { clearFocus(); dateEnabled = true },
                    onValueChange = { date = it },
                )
            }
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