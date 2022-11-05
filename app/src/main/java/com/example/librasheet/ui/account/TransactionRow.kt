package com.example.librasheet.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier.weight(10f).padding(end = 10.dp)
        ) {
            Text(
                text = transaction.name,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
            )
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatDollar(transaction.value),
                color =
                    if (transaction.value < 0) MaterialTheme.colors.error
                    else MaterialTheme.colors.primary,
            )
            Text(
                text= formatDateInt(transaction.date),
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
            )

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