package com.example.librasheet.ui.transaction

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.librasheet.data.entity.*
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.data.toIntDate
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.components.selectors.AccountSelector
import com.example.librasheet.ui.components.selectors.CategorySelector
import com.example.librasheet.ui.components.selectors.DropdownOptions
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.*
import java.text.SimpleDateFormat


private enum class ReimbursementOptions(override val displayName: String): HasDisplayName {
    VALUE("Change Amount"),
    DELETE("Delete"),
}
private enum class AllocationOptions(override val displayName: String): HasDisplayName {
    EDIT("Edit"),
    DELETE("Delete"),
}
private val reimbursementOptions = ImmutableList(ReimbursementOptions.values().toList())
private val allocationOptions = ImmutableList(AllocationOptions.values().toList())


@SuppressLint("SimpleDateFormat")
@Composable
fun TransactionDetailScreen(
    account: MutableState<Account?>,
    category: MutableState<Category?>,
    name: MutableState<String>,
    date: MutableState<String>,
    value: MutableState<String>,
    dateError: State<Boolean>,
    valueError: State<Boolean>,
    reimbursements: SnapshotStateList<ReimbursementWithValue>,
    allocations: SnapshotStateList<Allocation>,
    accounts: SnapshotStateList<Account>,
    incomeCategories: SnapshotStateList<Category>,
    expenseCategories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onBack: () -> Unit = { },
    onSave: () -> Boolean = { false },
    onAddReimbursement: () -> Unit = { },
    onDeleteReimbursement: (Int) -> Unit = { },
    onChangeReimbursementValue: (Int) -> Unit = { },
    onAddAllocation: () -> Unit = { },
    onEditAllocation: (Int) -> Unit = { },
    onDeleteAllocation: (Int) -> Unit = { },
    onReorderAllocation: (Int, Int) -> Unit = { _, _ -> },
) {
    val focusManager = LocalFocusManager.current

    val categoryList by remember { derivedStateOf {
        if ((value.value.toFloatOrNull() ?: 0f) > 0f) incomeCategories else expenseCategories
    } }

    fun saveTransaction() {
        val ok = onSave()
        if (ok) onBack()
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

        DragHost {
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

                editor("Date", date, error = dateError.value, placeholder = "mm-dd-yy")

                editor("Value", value, error = valueError.value)


                item("Category") {
                    LabeledRow(
                        label = "Category",
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        CategorySelector(
                            selection = category.value,
                            options = categoryList,
                            delayOpen = 100,
                            onSelection = { category.value = it },
                            modifier = Modifier.padding(start = 6.dp) // to match the padding of the editors between the box and the text
                        )
                    }
                }

                item("Allocations") {
                    RowTitle("Allocations", Modifier.padding(top = 20.dp)) {
                        IconButton(onClick = onAddAllocation) {
                            Icon(imageVector = Icons.Sharp.Add, contentDescription = null)
                        }
                    }
                }

                itemsIndexed(allocations) { i, allocation ->
                    if (i > 0) RowDivider(Modifier.zIndex(1f))
                    DragToReorderTarget(
                        index = i,
                        onDragEnd = { _, start, end -> onReorderAllocation(start, end) },
                    ) {
                        AllocationRow(allocation) {
                            DropdownOptions(options = allocationOptions) {
                                when (it) {
                                    AllocationOptions.DELETE -> { onDeleteAllocation(i) }
                                    AllocationOptions.EDIT -> { onEditAllocation(i) }
                                }
                            }
                        }
                    }
                }


                item("Reimbursements") {
                    RowTitle("Reimbursements", Modifier.padding(top = 20.dp)) {
                        IconButton(onClick = onAddReimbursement) {
                            Icon(imageVector = Icons.Sharp.Add, contentDescription = null)
                        }
                    }
                }

                itemsIndexed(reimbursements) { i, it ->
                    if (i > 0) RowDivider()
                    ReimbursementRow(
                        r = it,
                        accounts = accounts,
                    ) {
                        DropdownOptions(options = reimbursementOptions) {
                            when (it) {
                                ReimbursementOptions.DELETE -> { onDeleteReimbursement(i) }
                                ReimbursementOptions.VALUE -> { onChangeReimbursementValue(i) }
                            }
                        }
                    }
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
}




@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionDetailScreen(
                account = previewAccountNull,
                category = previewCategory,
                name = remember { mutableStateOf("TARGET STORED ID:XXXXXX") },
                date = remember { mutableStateOf("11-15-22") },
                value = remember { mutableStateOf("-26.34") },
                dateError = remember { mutableStateOf(false) },
                valueError = remember { mutableStateOf(true) },
                reimbursements = previewReimbursements,
                allocations = previewAllocations,
                accounts = previewAccounts,
                incomeCategories = previewIncomeCategories2,
                expenseCategories = previewIncomeCategories2,
            )
        }
    }
}