package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.screenReader.ScreenReader
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.settings.cards.AccountCard
import com.example.librasheet.ui.settings.cards.BackupDatabaseCard
import com.example.librasheet.ui.settings.cards.CategoryCard
import com.example.librasheet.ui.settings.cards.TransactionCard
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    screenReaderSize: Int = ScreenReader.list.size, // this needs to be here to fix the preview
    toEditAccounts: () -> Unit = { },
    toEditCategories: () -> Unit = { },
    toCategoryRules: (income: Boolean) -> Unit = { },
    toAddTransaction: () -> Unit = { },
    toAddCSV: () -> Unit = { },
    toScreenReader: () -> Unit = { },
    toAllTransactions: () -> Unit = { },
    onBackupDatabase: () -> Unit = { },
) {
    Column(modifier) {
        HeaderBar(title = "Settings")

        LazyColumn {
            item(key = "accounts") {
                AccountCard(
                    toEditAccounts = toEditAccounts,
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
                    toScreenReader = toScreenReader,
                    toAllTransactions = toAllTransactions,
                    screenReaderSize = screenReaderSize,
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
                screenReaderSize = 13, // this needs to be here to fix the preview
            )
        }
    }
}