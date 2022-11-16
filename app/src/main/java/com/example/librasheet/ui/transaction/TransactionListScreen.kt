package com.example.librasheet.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FilterAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.dao.TransactionFilters
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.data.entity.find
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowDivider
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.components.formatDateIntSimple
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewIncomeCategories2
import com.example.librasheet.viewModel.preview.previewTransactions

@Composable
fun TransactionListScreen(
    filter: State<TransactionFilters>,
    transactions: SnapshotStateList<TransactionEntity>,
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onFilter: () -> Unit = { },
    onTransactionClick: (TransactionEntity) -> Unit = { },
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HeaderBar(
            title = "Transactions",
            backArrow = true,
            onBack = onBack,
        ) {
            Spacer(Modifier.weight(10f))
            IconButton(onClick = onFilter) {
                Icon(Icons.Sharp.FilterAlt, null)
            }
        }

        TransactionFilterHeader(filter = filter, accounts = accounts)

        LazyColumn(
            contentPadding = PaddingValues(top = 10.dp)
        ) {
            itemsIndexed(transactions) { index, t ->
                if (index > 0) RowDivider()

                TransactionRow(
                    transaction = t,
                    modifier = Modifier
                        .clickable { onTransactionClick(t) }
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
            TransactionListScreen(
                transactions = previewTransactions,
                accounts = previewAccounts,
                filter = remember { mutableStateOf(TransactionFilters(
                    startDate = 20221122,
                    endDate = 20221222,
                    limit = 1000,
                    account = 1,
                    category = previewIncomeCategories2[0],
                )) },
            )
        }
    }
}