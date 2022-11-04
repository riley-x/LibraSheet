package com.example.librasheet.ui.balance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.DialSelector
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.graphing.PieChart
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.preview.previewAccounts


val tabs = listOf("Pie Chart", "History")


@Composable
fun BalanceScreen(
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
    ) {
        HeaderBar(title = "Balances")

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
                            accounts = accounts,
                            modifier = Modifier
                                .padding(start = 30.dp, end = 30.dp)
                        )
                        else -> { }
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
                accounts = previewAccounts
            )
        }
    }
}