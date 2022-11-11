package com.example.librasheet.ui.settings.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme


@Composable
fun AccountCard(
    modifier: Modifier = Modifier,
    toEditAccounts: () -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            CardTitle(title = "Accounts")
            CardRowDivider(color = MaterialTheme.colors.primary)
            ClickableRow("Edit", toEditAccounts)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            AccountCard()
        }
    }
}