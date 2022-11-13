package com.example.librasheet.ui.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.FilterAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowDivider
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewTransactions

@Composable
fun TransactionListScreen(
    transactions: SnapshotStateList<TransactionEntity>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onFilter: () -> Unit = { },
    onTransactionClick: (TransactionEntity) -> Unit = { },
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HeaderBar(
            title = "Transactions",
            backArrow = true,
            onBack = onBack,
        ) {
            Spacer(Modifier.weight(10f))
            IconButton(onClick = onFilter) {
                Icon(Icons.Sharp.FilterAlt, null)
            }
        }

        LazyColumn {
            itemsIndexed(transactions) { index, t ->
                if (index > 0) RowDivider()

                TransactionRow(
                    transaction = t,
                    modifier = Modifier
                        .clickable { onTransactionClick(t) }
                )
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionListScreen(transactions = previewTransactions)
        }
    }
}