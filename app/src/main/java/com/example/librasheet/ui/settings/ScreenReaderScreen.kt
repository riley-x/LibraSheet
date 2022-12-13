package com.example.librasheet.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.librasheet.data.entity.Account
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowDivider
import com.example.librasheet.ui.components.libraRowHorizontalPadding
import com.example.librasheet.ui.components.selectors.AccountSelector
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.ui.transaction.TransactionRow
import com.example.librasheet.viewModel.ScreenReaderAccountState
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewTransactionWithDetails
import com.example.librasheet.viewModel.preview.previewTransactions

val supportedApps = buildAnnotatedString {
    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
        append("Bank of America: ")
    }
    append("navigate to the \"All transactions\" page for any account.\n\n")
    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
        append("Chase: ")
    }
    append("navigate to the \"See all transactions\" page for any account.\n\n")
    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
        append("Venmo: ")
    }
    append("navigate to the \"Me\" tab, select the magnifying glass, and select \"Filter -> By payment method -> Venmo balance\"\n")
}


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
    onClear: () -> Unit = { },
    onSave: () -> Unit = { },
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HeaderBar(
            title = "Screen Reader",
            backArrow = true,
            onBack = onBack,
        )

        if (transactions.isEmpty()) {
            Column(
                Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                    textAlign = TextAlign.Justify,
                    text = "Libra Sheet can automatically record transactions from your banking apps. " +
                            "Whenever you view your transactions inside another app, Libra Sheet will parse them as you scroll. " +
                            "Return to this screen to edit and save the parsed transactions. " +
                            "Currently the supported apps are:\n",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp, end = 20.dp)
                ) {
                    Text(
                        color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                        text = supportedApps,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                    textAlign = TextAlign.Justify,
                    text = "Note that you must enable this service via the Android settings menu in " +
                            "\"Accessibility -> Installed services\".",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                // TODO add button to open settings?
                //  https://stackoverflow.com/questions/10061154/how-to-programmatically-enable-disable-accessibility-service-in-android
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(10f)
            ) {
                transactions.forEachIndexed { iAccount, it ->
                    if (iAccount > 0) item {
                        Spacer(Modifier.height(40.dp))
                    }

                    stickyHeader {
                        ScreenReaderAccountHeader(
                            state = it,
                            accounts = accounts,
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
                                transaction = t.transaction,
                                modifier = Modifier.clickable { onClickTransaction(iAccount, iTransaction) }
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
            ) {
                Button(
                    onClick = onClear,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error,
                    ),
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("Clear", fontSize = 18.sp)
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("Save", fontSize = 18.sp)
                }
            }
        }
    }
}


@Composable
fun ScreenReaderAccountHeader(
    state: ScreenReaderAccountState,
    accounts: SnapshotStateList<Account>,
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
                    text = if (state.parsedAccountName.isNotEmpty()) "Account: ${state.parsedAccountName}" else "Account",
                    style = MaterialTheme.typography.body2,
                    maxLines = 1,
                )

                AccountSelector(
                    selection = state.account,
                    options = accounts,
                    onSelection = onAccountSelection,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Invert",
                    style = MaterialTheme.typography.body2,
                )

                Checkbox(
                    checked = state.inverted,
                    onCheckedChange = onInvertValues,
                )
            }

        }
    }
}

@Preview
@Composable
private fun PreviewMessage() {
    val t = remember { mutableStateListOf<ScreenReaderAccountState>() }
    LibraSheetTheme {
        Surface {
            ScreenReaderScreen(transactions = t, accounts = previewAccounts)
        }
    }
}



@Preview
@Composable
private fun Preview() {
    val t = remember { mutableStateListOf(
        ScreenReaderAccountState(
            account = previewAccounts[0],
            parsedAccountName = "",
            transactions = previewTransactionWithDetails,
            inverted = false,
        ),
        ScreenReaderAccountState(
            account = null,
            parsedAccountName = "x1234",
            transactions = previewTransactionWithDetails,
            inverted = true,
        ),
    ) }
    LibraSheetTheme {
        Surface {
            ScreenReaderScreen(transactions = t, accounts = previewAccounts)
        }
    }
}