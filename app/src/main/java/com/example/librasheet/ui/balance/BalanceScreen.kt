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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils.clamp
import com.example.librasheet.ui.components.DialSelector
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowDivider
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.graphing.*
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewEmptyStringList
import com.example.librasheet.viewModel.preview.previewStackedLineGraph
import com.example.librasheet.viewModel.preview.previewStackedLineGraphAxes
import kotlin.math.roundToInt


private val tabs = listOf("Pie Chart", "History")


@Composable
fun BalanceScreen(
    accounts: SnapshotStateList<Account>,
    historyAxes: State<AxesState>,
    history: State<StackedLineGraphValues>,
    historyDates: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onAccountClick: (Account) -> Unit = { },
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> PieChart(
                            values = accounts,
                            modifier = Modifier
                                .padding(start = 30.dp, end = 30.dp)
                        )
                        else -> {
                            val hoverLoc = remember { mutableStateOf(-1) }
                            val showHover by remember { derivedStateOf { hoverLoc.value >= 0 } }
                            val graph = stackedLineGraphDrawer(values = history)
                            val graphHover = stackedLineGraphHover(values = history, hoverLoc = hoverLoc)
                            fun onHover(isHover: Boolean, x: Float, y: Float) {
                                if (history.value.isEmpty()) return
                                if (isHover) {
                                    hoverLoc.value = clamp(x.roundToInt(), 0, history.value.first().second.lastIndex)
                                    hoverText = formatDollar(history.value.first().second[hoverLoc.value]) + "\n" + historyDates[hoverLoc.value]
                                } else {
                                    hoverLoc.value = -1
                                    hoverText = ""
                                }
                            }
                            Graph(
                                axesState = historyAxes,
                                contentBefore = graph,
                                contentAfter = { if (showHover) graphHover(it) },
                                onHover = ::onHover,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }

            item("selector") {
                DialSelector(
                    selectedIndex = selectedTab,
                    labels = tabs,
                    onSelection = { selectedTab = it },
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                )
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
                historyAxes = previewStackedLineGraphAxes,
                history = previewStackedLineGraph,
                historyDates = previewEmptyStringList,
            )
        }
    }
}