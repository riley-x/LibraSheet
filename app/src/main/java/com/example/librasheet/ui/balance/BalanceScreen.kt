package com.example.librasheet.ui.balance

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
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.preview.*


private val tabs = ImmutableList(listOf("Pie Chart", "History", "Net Income"))


@Composable
fun BalanceScreen(
    accounts: SnapshotStateList<Account>,
    history: StackedLineGraphState,
    netIncome: DiscreteGraphState,
    dates: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onAccountClick: (Account) -> Unit = { },
) {
    val selectedTab = rememberSaveable { mutableStateOf(0) }
    var hoverText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        HeaderBar(title = "Balances") {
            Spacer(Modifier.weight(10f))
            Text(hoverText, textAlign = TextAlign.End)
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
                        0 -> PieChart(
                            values = accounts,
                            modifier = Modifier
                                .padding(start = 30.dp, end = 30.dp)
                        )
                        1 -> StackedLineGraph(
                            state = history,
                            modifier = Modifier.fillMaxSize()
                        ) { isHover, loc ->
                            hoverText = if (isHover)
                                formatDollar(history.values.first().second[loc]) + "\n" + dates[loc]
                            else ""
                        }
                        else -> BinaryBarGraph(
                            state = netIncome,
                            modifier = Modifier.fillMaxSize()
                        ) { isHover, loc ->
                            hoverText = if (isHover)
                                formatDollar(netIncome.values[loc]) + "\n" + dates[loc]
                            else ""
                        }
                    }
                }
            }

            itemsIndexed(accounts) { index, account ->
                if (index > 0) RowDivider()
                
                BalanceRow(
                    account = account,
                    modifier = Modifier
                        .clickable { onAccountClick(account) }
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
            BalanceScreen(
                accounts = previewAccounts,
                history = previewStackedLineGraphState,
                dates = previewEmptyStringList,
                netIncome = previewNetIncomeState,
            )
        }
    }
}