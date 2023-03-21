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
import androidx.compose.ui.zIndex
import com.example.librasheet.data.entity.Account
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.components.selectors.GraphSelector
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dragGroupAssets
import com.example.librasheet.viewModel.dragGroupLiabilities
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.*


private val tabs = ImmutableList(listOf("Current", "History", "Net Income"))


@Composable
fun BalanceScreen(
    accounts: SnapshotStateList<Account>,
    liabilities: SnapshotStateList<Account>,
    history: StackedLineGraphState,
    netIncome: DiscreteGraphState,
    historyDates: SnapshotStateList<String>,
    incomeDates: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onAccountClick: (Account) -> Unit = { },
    onReorder: (group: String, startIndex: Int, endIndex: Int) -> Unit = { _, _, _ -> },
) {
    val selectedTab = rememberSaveable { mutableStateOf(0) }
    var hoverText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        HeaderBar(title = "Balances", modifier = Modifier.zIndex(1f)) {
            Spacer(Modifier.weight(10f))
            Text(hoverText, textAlign = TextAlign.End)
        }

        DragHost {
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
                            0 -> PieChartFiltered(
                                values = accounts,
                                modifier = Modifier
                                    .padding(start = 30.dp, end = 30.dp)
                            )
                            1 -> StackedLineGraph(
                                state = history,
                                modifier = Modifier.fillMaxSize()
                            ) { isHover, loc ->
                                hoverText = if (isHover)
                                    formatDollar(history.values.first().second[loc]) + "\n" +
                                            historyDates.getOrElse(loc) { "" }
                                else ""
                            }
                            else -> BinaryBarGraph(
                                state = netIncome,
                                modifier = Modifier.fillMaxSize()
                            ) { isHover, loc ->
                                hoverText = if (isHover)
                                    formatDollar(netIncome.values[loc]) + "\n" +
                                            incomeDates.getOrElse(loc) { "" }
                                else ""
                            }
                        }
                    }
                }

                itemsIndexed(accounts) { index, account ->
                    if (index > 0) RowDivider(Modifier.zIndex(1f))

                    DragToReorderTarget(
                        index = index,
                        group = dragGroupAssets,
                        onDragEnd = onReorder,
                    ) {
                        BalanceRow(
                            account = account,
                            modifier = Modifier
                                .clickable { onAccountClick(account) }
                        )
                    }
                }

                item {
                    if (liabilities.isNotEmpty()) {
                        RowTitle(
                            title = "Liabilities",
                            modifier = Modifier.padding(top = 20.dp)
                        )
                    }
                }

                itemsIndexed(liabilities) { index, account ->
                    if (index > 0) RowDivider(Modifier.zIndex(1f))

                    DragToReorderTarget(
                        index = index,
                        group = dragGroupLiabilities,
                        onDragEnd = onReorder,
                    ) {
                        BalanceRow(
                            account = account,
                            modifier = Modifier
                                .clickable { onAccountClick(account) }
                        )
                    }
                }
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
                liabilities = previewAccountsLiabilities,
                history = previewStackedLineGraphState,
                historyDates = previewEmptyStringList,
                incomeDates = previewEmptyStringList,
                netIncome = previewBarState,
            )
        }
    }
}