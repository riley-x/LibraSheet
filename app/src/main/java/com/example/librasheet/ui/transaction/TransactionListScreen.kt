package com.example.librasheet.ui.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewTransactions

@Composable
fun TransactionScreen(
    transactions: SnapshotStateList<TransactionEntity>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
) {
    Column(
        modifier = modifier
    ) {
        HeaderBar(
            title = "Transactions",
            backArrow = true,
            onBack = onBack,
        )
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
//            TransactionScreen(transactions = previewTransactions)
        }
    }
}