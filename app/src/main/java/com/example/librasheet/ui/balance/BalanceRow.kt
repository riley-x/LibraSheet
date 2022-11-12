package com.example.librasheet.ui.balance

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewAccounts

@Composable
fun BalanceRow(
    account: Account,
    modifier: Modifier = Modifier
) {
    ColorCodedRow(color = account.color, modifier = modifier) {
        Text(account.name)
        Spacer(modifier = Modifier.weight(10f))
        Text(formatDollar(account.balance))
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            BalanceRow(account = previewAccounts[0])
        }
    }
}
