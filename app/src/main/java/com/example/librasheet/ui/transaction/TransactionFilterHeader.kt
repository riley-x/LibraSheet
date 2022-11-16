package com.example.librasheet.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.find
import com.example.librasheet.ui.components.formatDateIntSimple
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewIncomeCategories2
import com.example.librasheet.viewModel.preview.previewTransactionFilters
import com.example.librasheet.viewModel.preview.previewTransactions

@Composable
fun ColumnScope.TransactionFilterHeader(
    filter: State<TransactionFilters>,
    accounts: SnapshotStateList<Account>,
) {
    Divider(
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    )
    Row(
        Modifier
            .padding(vertical = 3.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Account: " + (accounts.find(filter.value.account)?.name ?: "-"),
            maxLines = 1,
            style = MaterialTheme.typography.caption,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(10f)
                .padding(start = 10.dp)
        )
        Text(
            text = "Category: " + (filter.value.category?.id?.name ?: "-"),
            maxLines = 1,
            style = MaterialTheme.typography.caption,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(10f)
                .padding(start = 10.dp)
        )
    }
    Row(
        Modifier
            .padding(vertical = 3.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Dates: " +
                    (if (filter.value.startDate != null) formatDateIntSimple(filter.value.startDate!!) else "") +
                    " - " +
                    (if (filter.value.endDate != null) formatDateIntSimple(filter.value.endDate!!) else ""),
            maxLines = 1,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .weight(10f)
                .padding(start = 10.dp)
        )
        Text(
            text = "Limit: " + if (filter.value.limit != null) "${filter.value.limit}" else "",
            maxLines = 1,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .weight(10f)
                .padding(start = 10.dp)
        )
    }
    Divider(
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    )
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Column {
                TransactionFilterHeader(
                    accounts = previewAccounts,
                    filter = previewTransactionFilters,
                )
            }
        }
    }
}