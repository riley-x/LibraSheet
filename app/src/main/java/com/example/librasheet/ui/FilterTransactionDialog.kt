package com.example.librasheet.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.toIntDate
import com.example.librasheet.ui.components.formatDateIntSimple
import com.example.librasheet.ui.components.parseOrNull
import com.example.librasheet.ui.dialogs.Dialog
import com.example.librasheet.ui.settings.CategoryRuleDialog
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewIncomeCategories2
import com.example.librasheet.viewModel.preview.previewTransactionFilters
import java.text.SimpleDateFormat

class FilterTransactionDialogHolder(
    private val viewModel: LibraViewModel,
    private val navController: NavController,
) {
    var isOpen by mutableStateOf(false)
    private var isSettings = false
    private var currentFilters = mutableStateOf(TransactionFilters())

    fun openSettings() {
        isOpen = true
        isSettings = true
        currentFilters = viewModel.transactions.settingsFilter
    }

    fun openBalance() {
        isOpen = true
        isSettings = false
        currentFilters = viewModel.transactions.balanceFilter
    }

    fun cancel() {
        isOpen = false
        navController.popBackStack()
    }

    fun save(filters: TransactionFilters) {
        isOpen = false
        if (isSettings) viewModel.transactions.filterSettings(filters)
        else viewModel.transactions.filterBalance(filters)
    }

    @Composable
    fun Content() {
        if (isOpen) {
            FilterTransactionDialog(
                filters = currentFilters,
                accounts = viewModel.accounts.all,
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
        title = "Filters",
        onCancel = onCancel,
        onOk = ::onOk,
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text(text = "Date from:") },
        )
    }
}




@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        FilterTransactionDialog(
            filters = previewTransactionFilters,
            accounts = previewAccounts,
        )
    }
}
