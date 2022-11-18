package com.example.librasheet.ui.transaction

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.data.toIntDate
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.components.selectors.AccountSelector
import com.example.librasheet.ui.components.selectors.CategorySelector
import com.example.librasheet.ui.components.selectors.DropdownSelector
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.TransactionWithDetails
import com.example.librasheet.viewModel.preview.*
import java.text.SimpleDateFormat


@SuppressLint("SimpleDateFormat")
@Composable
fun TransactionDetailScreen(
    state: TransactionWithDetails,
    accounts: SnapshotStateList<Account>,
    incomeCategories: SnapshotStateList<Category>,
    expenseCategories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit = { },
    onSave: (new: TransactionEntity, old: TransactionEntity) -> Unit = { _, _ -> },
    onAddReimbursement: (TransactionWithDetails) -> Unit = { }, // TODO make sure this can handle a new transaction
) {
    val focusManager = LocalFocusManager.current

    val formatter = remember {
        val x = SimpleDateFormat("MM-dd-yy")
        x.isLenient = false
        x
    }

    // TODO make account and category saveable
    val account = remember { mutableStateOf(accounts.find { it.key == state.transaction.value.accountKey }) }
    val name = rememberSaveable { mutableStateOf(state.transaction.value.name) }
    val date = rememberSaveable { mutableStateOf(formatDateIntSimple(state.transaction.value.date, "-")) }
    val value = rememberSaveable { mutableStateOf(
        if (state.transaction.value.value == 0L) ""
        else state.transaction.value.value.toFloatDollar().toString())
    }

    var dateError by remember { mutableStateOf(false) }
    var valueError by remember { mutableStateOf(false) }

    val categoryList by remember { derivedStateOf {
        if ((value.value.toFloatOrNull() ?: 0f) > 0f) incomeCategories else expenseCategories
    } }
    val category = remember { mutableStateOf(
        (if (state.transaction.value.value > 0) incomeCategories else expenseCategories)
            .find { it.key == state.transaction.value.categoryKey }
    ) }

    fun saveTransaction() {
        val dateInt = formatter.parseOrNull(date.value)?.toIntDate()
        val valueLong = value.value.toFloatOrNull()?.toLongDollar()

        dateError = dateInt == null
        valueError = valueLong == null
        if (dateError || valueError) return

        val t = TransactionEntity(
            key = state.transaction.value.key,
            name = name.value,
            date = dateInt ?: 0,
            value = valueLong ?: 0L,
            category = category.value ?: Category.None,
            categoryKey = category.value?.key ?: 0,
            accountKey = account.value?.key ?: 0,
//            valueAfterReimbursements = // TODO,
        )
        onSave(t, state.transaction.value)
        onBack()
    }



    fun LazyListScope.editor(
        label: String,
        text: MutableState<String>,
        number: Boolean = true,
        error: Boolean = false,
        placeholder: String = "",
        onValueChange: (String) -> Unit = { text.value = it },
    ) {
        item(label) {
            TransactionEditRow(
                label = label,
                text = text.value,
                number = number,
                error = error,
                placeholder = placeholder,
                onValueChange = onValueChange,
            )
        }
    }

    val imePadding = with(LocalDensity.current) {
        WindowInsets.ime.getBottom(this).toDp()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = maxOf(bottomPadding, imePadding))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { focusManager.clearFocus(true) }
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
                LabeledRow(
                    label = "Account",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                ) {
                    AccountSelector(
                        selection = account.value,
                        options = accounts,
                        onSelection = { account.value = it },
                        modifier = Modifier.padding(start = 6.dp) // to match the padding of the editors between the box and the text
                    )
                }
            }

            editor("Name", name, number = false)

            editor("Date", date, error = dateError, placeholder = "mm-dd-yy")

            editor("Value", value, error = valueError)


            item("Category") {
                LabeledRow(
                    label = "Category",
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CategorySelector(
                        selection = category.value,
                        options = categoryList,
                        onSelection = { category.value = it },
                        modifier = Modifier.padding(start = 6.dp) // to match the padding of the editors between the box and the text
                    )
                }
            }


            item("Reimbursements") {
                RowTitle("Reimbursements", Modifier.padding(top = 20.dp)) {
                    IconButton(onClick = { onAddReimbursement(state) }) {
                        Icon(imageVector = Icons.Sharp.Add, contentDescription = null)
                    }
                }
            }

            itemsIndexed(state.reimbursements) { i, it ->
                if (i > 0) RowDivider()
                ReimbursementRow(
                    r = it,
                    accounts = accounts,
                )
            }

            item("Allocations") {
                RowTitle("Allocations", Modifier.padding(top = 20.dp)) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Sharp.Add, contentDescription = null)
                    }
                }
            }

            itemsIndexed(state.allocations) { i, it ->
                if (i > 0) RowDivider()
                AllocationRow(it)
            }


            item("Buttons") {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
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
    }
}




@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionDetailScreen(
                state = previewTransactionDetail,
                accounts = previewAccounts,
                incomeCategories = previewIncomeCategories2,
                expenseCategories = previewIncomeCategories2,
            )
        }
    }
}