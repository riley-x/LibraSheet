package com.example.librasheet.ui.settings.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.screenReader.ScreenReader
import com.example.librasheet.ui.theme.LibraSheetTheme


@Composable
fun TransactionCard(
    modifier: Modifier = Modifier,
    screenReaderSize: Int = ScreenReader.list.size, // this needs to be here to fix the preview
    toAddTransaction: () -> Unit = { },
    toAddCSV: () -> Unit = { },
    toScreenReader: () -> Unit = { },
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
            ClickableRow("Screen Reader ($screenReaderSize)", toScreenReader)
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
            TransactionCard(
                screenReaderSize = 13, // this needs to be here to fix the preview
            )
        }
    }
}