package com.example.librasheet.ui.settings.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.components.DropdownOptions
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.settings.AccountCard
import com.example.librasheet.ui.settings.CardRowDivider
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.previewAccounts


private enum class AccountOptions(override val displayName: String): HasDisplayName {
    RENAME("Rename"),
    COLOR("Change Color"),
    DELETE("Delete"),
}
private val accountOptions = ImmutableList(AccountOptions.values().toList())


@Composable
fun EditAccountsScreen(
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onAddAccount: () -> Unit = { },
    onChangeName: (String) -> Unit = { },
    onChangeColor: (String) -> Unit = { },
    onDelete: (String) -> Unit = { },
) {
    Column(modifier) {
        HeaderBar(title = "Accounts", backArrow = true, onBack = onBack) {
            IconButton(onClick = onAddAccount) {
                Icon(Icons.Sharp.Add, null)
            }
        }

        LazyColumn {
            itemsIndexed(accounts) { i, account ->
                if (i > 0) CardRowDivider()

                ColorCodedRow(
                    color = account.color,
                ) {
                    Text(account.name, modifier = Modifier.weight(10f))
                    DropdownOptions(options = accountOptions) {
                        when (it) {
                            AccountOptions.RENAME -> onChangeName(account.name)
                            AccountOptions.COLOR -> onChangeColor(account.name)
                            AccountOptions.DELETE -> onDelete(account.name)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            EditAccountsScreen(
                accounts = previewAccounts,
            )
        }
    }
}