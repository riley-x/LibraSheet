package com.example.librasheet.ui.balance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.ui.transaction.TransactionRow
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.*

private val tabs = ImmutableList(listOf("Balance", "Net Income"))


@Composable
fun AccountScreen(
    account: State<Account>,
    balance: DiscreteGraphState,
    netIncome: NetIncomeGraphState,
    historyDates: SnapshotStateList<String>,
    incomeDates: SnapshotStateList<String>,
    transactions: SnapshotStateList<TransactionEntity>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onClickColor: (String) -> Unit = { },
    toTransaction: (TransactionEntity) -> Unit = { },
) {
    val selectedTab = rememberSaveable { mutableStateOf(0) }
    var hoverText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        HeaderBar(
            title = account.value.name,
            backArrow = true,
            onBack = onBack,
        ) {
            Spacer(Modifier.weight(10f))
            if (hoverText.isNotBlank()) {
                Text(hoverText, textAlign = TextAlign.End)
            } else {
                Canvas(
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .size(30.dp)
                        .clickable { onClickColor("account_" + account.value.name) }
                ) {
                    drawRect(color = account.value.color)
                }
            }
        }

        LazyColumn {
            item("graphic") {
                GraphSelector(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onSelection = { selectedTab.value = it },
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    when (it) {
                        0 -> LineGraph(
                            state = balance,
                            modifier = Modifier.fillMaxSize()
                        ) { isHover, loc ->
                            hoverText = if (isHover)
                                formatDollar(balance.values[loc]) + "\n" + historyDates[loc]
                            else ""
                        }
                        1 -> NetIncomeGraph(
                            state = netIncome,
                            modifier = Modifier.fillMaxSize()
                        ) { isHover, loc ->
                            hoverText = if (isHover)
                                formatDollar(netIncome.valuesNet[loc]) + "\n" + incomeDates[loc]
                            else ""
                        }
                    }
                }
            }

            itemsIndexed(transactions) { index, t ->
                if (index > 0) RowDivider()

                TransactionRow(
                    transaction = t,
                    modifier = Modifier
                        .clickable { toTransaction(t) }
                )
            }
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
)
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            AccountScreen(
                account = previewAccount,
                incomeDates = previewLineGraphDates,
                historyDates = previewLineGraphDates,
                balance = previewLineGraphState,
                netIncome = previewNetIncomeState,
                transactions = previewTransactions,
            )
        }
    }
}