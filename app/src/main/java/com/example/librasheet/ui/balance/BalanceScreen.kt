package com.example.librasheet.ui.balance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.graphing.PieChart
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.Account
import com.example.librasheet.viewModel.preview.previewAccounts

@Composable
fun BalanceScreen(
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        HeaderBar(title = "Balances")

        LazyColumn {
            item("graphic") {
                PieChart(
                    accounts = accounts,
                    modifier = Modifier
                        .padding(start = 30.dp, end = 30.dp)
                        .heightIn(0.dp, 300.dp)
                        .fillMaxWidth(),
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