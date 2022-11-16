package com.example.librasheet.ui.dialogHolders

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.example.librasheet.data.toIntDate
import com.example.librasheet.ui.components.formatDateIntSimple
import com.example.librasheet.ui.components.parseOrNull
import com.example.librasheet.ui.dialogs.Dialog
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.preview.previewAccounts
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
        currentFilters = viewModel.transactions.settingsFilter
    }

    fun openBalance() {
        isOpen = true
        isSettings = false
        currentFilters = viewModel.transactions.balanceFilter
    }

    fun cancel() {
        isOpen = false
    }

    fun save(filters: TransactionFilters) {
        isOpen = false
        if (isSettings) viewModel.transactions.filterSettings(filters)
        else viewModel.transactions.filterBalance(filters)
    }

    @Composable
    override fun Content() {
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
        onCancel = onCancel,
        onOk = ::onOk,
        modifier = modifier,
    ) {
        Row {
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text(text = "Date start:") },
                modifier = Modifier.weight(10f),
            )
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text(text = "Date end:") },
                modifier = Modifier.weight(10f),
            )
        }
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
