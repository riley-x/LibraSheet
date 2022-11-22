package com.example.librasheet.ui.transaction

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.toIntDate
import com.example.librasheet.ui.components.DialogHolder
import com.example.librasheet.ui.components.textFields.DateTextField
import com.example.librasheet.ui.components.formatDateIntSimple
import com.example.librasheet.ui.components.parseOrNull
import com.example.librasheet.ui.components.selectors.AccountSelector
import com.example.librasheet.ui.components.selectors.CategorySelector
import com.example.librasheet.ui.components.textFields.NumberTextField
import com.example.librasheet.ui.components.textFields.textFieldBorder
import com.example.librasheet.ui.dialogs.Dialog
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewIncomeCategories2
import com.example.librasheet.viewModel.preview.previewTransactionFilters
import java.text.SimpleDateFormat

class FilterTransactionDialogHolder(
    private val viewModel: LibraViewModel,
): DialogHolder {
    override var isOpen by mutableStateOf(false)
    private var isSettings = false
    private var currentFilters = mutableStateOf(TransactionFilters())

    fun openSettings() {
        isOpen = true
        isSettings = true
        currentFilters = viewModel.transactionsSettings.filter
    }

    fun openBalance() {
        isOpen = true
        isSettings = false
        currentFilters = viewModel.transactionsBalance.filter
    }

    fun cancel() {
        isOpen = false
    }

    fun save(filters: TransactionFilters) {
        isOpen = false
        if (isSettings) viewModel.transactionsSettings.filter(filters)
        else viewModel.transactionsBalance.filter(filters)
    }

    @Composable
    override fun Content() {
        if (isOpen) {
            FilterTransactionDialog(
                filters = currentFilters,
                accounts = viewModel.accounts.all,
                categories = viewModel.categories.allFilters,
                onCancel = ::cancel,
                onSave = ::save,
            )
        }
    }
}


@SuppressLint("SimpleDateFormat")
@Composable
fun FilterTransactionDialog(
    filters: State<TransactionFilters>,
    accounts: SnapshotStateList<Account>,
    categories: SnapshotStateList<Category>,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = { },
    onSave: (TransactionFilters) -> Unit = { },
) {
    var startDate by remember {
        mutableStateOf(filters.value.startDate?.let { formatDateIntSimple(it, "-") } ?: "")
    }
    var endDate by remember {
        mutableStateOf(filters.value.endDate?.let { formatDateIntSimple(it, "-") } ?: "")
    }
    var account by remember {
        mutableStateOf(accounts.find { it.key == filters.value.account })
    }
    var category by remember { mutableStateOf(filters.value.category) }
    var limit by remember { mutableStateOf(filters.value.limit?.toString() ?: "") }

    val formatter = remember {
        val x = SimpleDateFormat("MM-dd-yy")
        x.isLenient = false
        x
    }

    fun onOk() {
        onSave(
            TransactionFilters(
                startDate = formatter.parseOrNull(startDate)?.toIntDate(),
                endDate = formatter.parseOrNull(endDate)?.toIntDate(),
                account = account?.key,
                category = category,
                limit = limit.toIntOrNull(),
            )
        )
    }

    Dialog(
        onCancel = onCancel,
        onOk = ::onOk,
        modifier = modifier,
    ) {

        Row {
            DateTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = "Date Start",
                modifier = Modifier.weight(10f),
            )
            Spacer(modifier = Modifier.width(10.dp))
            DateTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = "Date End",
                modifier = Modifier.weight(10f),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        AccountSelector(
            selection = account,
            options = listOf(null) + accounts,
            onSelection = { account = it },
            modifier = Modifier
                .fillMaxWidth()
                .textFieldBorder("Account")
        )

        Spacer(modifier = Modifier.height(10.dp))

        CategorySelector(
            selection = category,
            options = categories,
            onSelection = { category = it },
            modifier = Modifier
                .fillMaxWidth()
                .textFieldBorder("Category")
        )

        Spacer(modifier = Modifier.height(10.dp))

        NumberTextField(
            value = limit,
            onValueChange = { limit = it },
            label = "Limit",
            modifier = Modifier.fillMaxWidth()
        )
    }
}




@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        FilterTransactionDialog(
            filters = previewTransactionFilters,
            categories = previewIncomeCategories2,
            accounts = previewAccounts,
        )
    }
}