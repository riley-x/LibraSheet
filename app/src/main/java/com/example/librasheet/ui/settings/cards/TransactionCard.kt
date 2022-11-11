package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme


@Composable
fun TransactionCard(
    modifier: Modifier = Modifier,
    toAddTransaction: () -> Unit = { },
    toAddCSV: () -> Unit = { },
    toAllTransactions: () -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            CardTitle(title = "Transactions")
            CardRowDivider(color = MaterialTheme.colors.primary)
            ClickableRow("Add Manual", toAddTransaction)
            CardRowDivider()
            ClickableRow("Add CSV", toAddCSV)
            CardRowDivider()
            ClickableRow("See All", toAllTransactions)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionCard()
        }
    }
}