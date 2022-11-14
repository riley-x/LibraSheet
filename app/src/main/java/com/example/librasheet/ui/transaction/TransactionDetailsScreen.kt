package com.example.librasheet.ui.transaction

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.data.entity.isValid
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.data.toIntDate
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewIncomeCategories2
import com.example.librasheet.viewModel.preview.previewTransaction
import com.example.librasheet.viewModel.preview.previewTransactions
import java.text.ParseException
import java.text.SimpleDateFormat


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionDetailScreen(
    transaction: State<TransactionEntity>,
    accounts: SnapshotStateList<Account>,
    incomeCategories: SnapshotStateList<Category>,
    expenseCategories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit = { },
    onSave: (new: TransactionEntity, old: TransactionEntity) -> Unit = { _, _ -> },
) {
    val focusManager = LocalFocusManager.current

    val formatter = remember {
        val x = SimpleDateFormat("MM-dd-yy")
        x.isLenient = false
        x
    }

    val account = remember { mutableStateOf(accounts.find { it.key == transaction.value.accountKey }) }
    val name = remember { mutableStateOf(transaction.value.name) }
    val date = remember { mutableStateOf(formatDateIntSimple(transaction.value.date, "-")) }
    val value = remember { mutableStateOf(transaction.value.value.toFloatDollar().toString()) }

    val dateError by remember { derivedStateOf { formatter.parseOrNull(date.value) == null } }
    val valueError by remember { derivedStateOf { value.value.toFloatOrNull() == null } }

    val categoryList by remember { derivedStateOf {
        if ((value.value.toFloatOrNull() ?: 0f) > 0f) incomeCategories else expenseCategories
    } }
    val category = remember { mutableStateOf(
        (if (transaction.value.value > 0) incomeCategories else expenseCategories)
            .find { it.key == transaction.value.categoryKey }
    ) }

    fun saveTransaction() {
        if (dateError || valueError) return
        val t = TransactionEntity(
            key = transaction.value.key,
            name = name.value,
            date = formatter.parseOrNull(date.value)?.toIntDate() ?: return,
            value = value.value.toFloatOrNull()?.toLongDollar() ?: return,
            category = category.value ?: Category.None,
            categoryKey = category.value?.key ?: 0,
            accountKey = account.value?.key ?: 0,
//            valueAfterReimbursements = // TODO,
        )
        onSave(t, transaction.value)
    }



    fun LazyListScope.editor(
        label: String,
        text: MutableState<String>,
        number: Boolean = true,
        error: Boolean = false,
        placeholder: String = "",
    ) {
        item(label) {
            TransactionEditRow(
                label = label,
                text = text.value,
                number = number,
                error = error,
                placeholder = placeholder,
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
                    onPress = { focusManager.clearFocus(true) }
                )
            }
    ) {
        HeaderBar(
            title = "Transactions",
            backArrow = true,
            onBack = onBack,
        )

        LazyColumn(
            Modifier.weight(10f)
        ) {
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
            editor("Date", date, error = dateError, placeholder = "mm-dd-yy")
            editor("Value", value, error = valueError)


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


            item("Reimbursements") {
                RowTitle("Reimbursements", Modifier.padding(top = 20.dp)) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Sharp.Add, contentDescription = null)
                    }
                }
            }

            item("Allocations") {
                RowTitle("Allocations", Modifier.padding(top = 20.dp)) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Sharp.Add, contentDescription = null)
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error
                ),
                onClick = onBack,
                modifier = Modifier.width(100.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = ::saveTransaction,
                modifier = Modifier.width(100.dp)
            ) {
                Text("Save")
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
                transaction = previewTransaction,
                accounts = previewAccounts,
                incomeCategories = previewIncomeCategories2,
                expenseCategories = previewIncomeCategories2,
            )
        }
    }
}