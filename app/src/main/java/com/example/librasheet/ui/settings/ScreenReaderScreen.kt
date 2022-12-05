package com.example.librasheet.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowDivider
import com.example.librasheet.ui.components.libraRowHorizontalPadding
import com.example.librasheet.ui.components.selectors.AccountSelector
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.ui.transaction.TransactionRow
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewTransactions


@Immutable
data class ScreenReaderAccountState(
    val account: Account,
    val transactions: List<TransactionEntity>,
    val inverted: Boolean,
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenReaderScreen(
    transactions: SnapshotStateList<ScreenReaderAccountState>,
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onAccountSelection: (Int, Account?) -> Unit = { _, _ -> },
    onInvertValues: (Int, Boolean) -> Unit = { _, _ -> },
    onClickTransaction: (iAccount: Int, iTransaction: Int) -> Unit = { _, _ -> },
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HeaderBar(
            title = "Screen Reader",
            backArrow = true,
            onBack = onBack,
        )

        LazyColumn(
            modifier = Modifier.weight(10f)
        ) {
            transactions.forEachIndexed { iAccount, it ->
                if (iAccount > 0) item {
                    Spacer(Modifier.height(40.dp))
                }

                stickyHeader {
                    ScreenReaderAccountHeader(
                        account = it.account,
                        accounts = accounts,
                        invertedValues = it.inverted,
                        onAccountSelection = { onAccountSelection(iAccount, it) },
                        onInvertValues = { onInvertValues(iAccount, it) }
                    )
                }

                itemsIndexed(it.transactions) { iTransaction, t ->
                    Column {
                        if (iTransaction > 0) {
                            RowDivider()
                        } else {
                            Spacer(Modifier.height(4.dp))
                        }

                        TransactionRow(
                            transaction = t,
                            modifier = Modifier.clickable { onClickTransaction(iAccount, iTransaction) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ScreenReaderAccountHeader(
    account: Account,
    accounts: SnapshotStateList<Account>,
    invertedValues: Boolean,
    modifier: Modifier = Modifier,
    onAccountSelection: (Account?) -> Unit = { },
    onInvertValues: (Boolean) -> Unit = { },
) {
    Surface(modifier,
        elevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = libraRowHorizontalPadding)
        ) {
            Column(
                modifier = Modifier.weight(10f)
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.subtitle1,
                )

                AccountSelector(
                    selection = account,
                    options = accounts,
                    onSelection = onAccountSelection,
                )
            }

            Column {
                Text(
                    text = "Invert",
                    style = MaterialTheme.typography.subtitle1,
                )

                Checkbox(
                    checked = invertedValues,
                    onCheckedChange = onInvertValues,
                )
            }

        }
    }
}



@Preview
@Composable
private fun PreviewHeader() {
    LibraSheetTheme {
        ScreenReaderAccountHeader(account = previewAccounts[0], accounts = previewAccounts, invertedValues = true)
    }
}


@Preview
@Composable
private fun Preview() {
    val t = remember { mutableStateListOf(
        ScreenReaderAccountState(
            account = previewAccounts[0],
            transactions = previewTransactions,
            inverted = false,
        ),
        ScreenReaderAccountState(
            account = previewAccounts[1],
            transactions = previewTransactions,
            inverted = true,
        ),
    ) }
    LibraSheetTheme {
        Surface {
            ScreenReaderScreen(transactions = t, accounts = previewAccounts)
        }
    }
}