package com.example.librasheet.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Transaction
import com.example.librasheet.viewModel.preview.previewTransactions

@Composable
fun TransactionRow(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    ColorCodedRow(color = transaction.color, modifier = modifier) {
        Text(transaction.name, style = MaterialTheme.typography.body2)
        Spacer(modifier = Modifier.weight(10f))
        Column {
            Text(formatDollar(transaction.value))
            Text(formatDateInt(transaction.date))

        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionRow(transaction = previewTransactions[0])
        }
    }
}