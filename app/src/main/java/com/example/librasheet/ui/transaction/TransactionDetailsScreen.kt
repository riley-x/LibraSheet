package com.example.librasheet.ui.transaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FilterAlt
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.data.entity.isValid
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.components.ColorIndicator
import com.example.librasheet.ui.components.DropdownOptions
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowTitle
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.previewAccount
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewIncomeCategories2
import com.example.librasheet.viewModel.preview.previewTransactions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp


@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TransactionDetailScreen(
    transaction: TransactionEntity,
    accounts: SnapshotStateList<Account>,
    incomeCategories: SnapshotStateList<Category>,
    expenseCategories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit = { },
) {
    /** Keyboard and focus **/
    val focusManager = LocalFocusManager.current

    val account = remember { mutableStateOf(accounts.find { it.key == transaction.accountKey }) }
    val name = remember { mutableStateOf(transaction.name) }
    val date = remember { mutableStateOf(transaction.date.toString()) }
    val value = remember { mutableStateOf(transaction.value.toFloatDollar().toString()) }

    val categoryList by remember { derivedStateOf {
        if ((value.value.toFloatOrNull() ?: 0f) > 0f) incomeCategories else expenseCategories
    } }
    val category = remember { mutableStateOf(
        (if (transaction.value > 0) incomeCategories else expenseCategories)
            .find { it.key == transaction.categoryKey }
    ) }

    fun clearFocus() {
        focusManager.clearFocus(true)
    }

    fun LazyListScope.editor(
        label: String,
        text: MutableState<String>,
        number: Boolean = true,
    ) {
        item(label) {
            TransactionEditRow(
                label = label,
                text = text.value,
                number = number,
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

            item("Account") {
                TransactionSelectorRow(
                    label = "Account",
                    selection = account.value,
                    options = accounts,
                    onSelection = { account.value = it },
                ) {
                    Spacer(Modifier.width(6.dp))
                    ColorIndicator(it?.color ?: Color.Unspecified)
                    Text(
                        text = it?.name ?: "None",
                        fontStyle = if (it == null) FontStyle.Italic else FontStyle.Normal,
                    )
                }
            }

            editor("Name", name, number = false)
            editor("Date", date)
            editor("Value", value)

            item("Category") {
                TransactionSelectorRow(
                    label = "Category",
                    selection = category.value,
                    options = categoryList,
                    onSelection = { category.value = it },
                ) {
                    Spacer(Modifier.width(6.dp))
                    ColorIndicator(it?.color ?: Color.Unspecified)
                    Text(
                        text = it?.id?.name ?: "None",
                        fontStyle = if (it.isValid()) FontStyle.Normal else FontStyle.Italic,
                        color = MaterialTheme.colors.onSurface.copy(
                            alpha = if (it.isValid()) ContentAlpha.high else ContentAlpha.medium,
                        )
                    )
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
            TransactionDetailScreen(
                transaction = previewTransactions[0],
                accounts = previewAccounts,
                incomeCategories = previewIncomeCategories2,
                expenseCategories = previewIncomeCategories2,
            )
        }
    }
}