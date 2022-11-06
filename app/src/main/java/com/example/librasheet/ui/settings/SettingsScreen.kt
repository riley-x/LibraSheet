package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.preview.previewAccounts

@Composable
fun SettingsScreen(
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier,
    onAddAccount: () -> Unit = { },
    onEditAccount: (Account) -> Unit = { },
    onSeeAllAccounts: () -> Unit = { },
    toEditCategories: () -> Unit = { },
    toCategoryRules: () -> Unit = { },
    toAddTransaction: () -> Unit = { },
    toAddCSV: () -> Unit = { },
    toAllTransactions: () -> Unit = { },
    onBackupDatabase: () -> Unit = { },
) {
    Column(modifier) {
        HeaderBar(title = "Settings")

        LazyColumn {
            item(key = "accounts") {
                AccountCard(
                    accounts = accounts,
                    onAddAccount = onAddAccount,
                    onEditAccount = onEditAccount,
                    onSeeAllAccounts = onSeeAllAccounts,
                    modifier = Modifier.padding(10.dp)
                )
            }

            item(key = "categories") {
                CategoryCard(
                    toEditCategories = toEditCategories,
                    toCategoryRules = toCategoryRules,
                    modifier = Modifier.padding(10.dp)
                )
            }

            item(key = "transactions") {
                TransactionCard(
                    toAddTransaction = toAddTransaction,
                    toAddCSV = toAddCSV,
                    toAllTransactions = toAllTransactions,
                    modifier = Modifier.padding(10.dp)
                )
            }

            item(key = "backup database") {
                BackupDatabaseCard(
                    onClick = onBackupDatabase,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
    showBackground = true,
)
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            SettingsScreen(
                accounts = previewAccounts,
            )
        }
    }
}