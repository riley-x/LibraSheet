package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.example.librasheet.data.entity.Account
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.components.selectors.DropdownOptions
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.previewAccounts


private enum class AccountOptions(override val displayName: String): HasDisplayName {
    EDIT("Edit"),
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
    onChangeName: (Int) -> Unit = { },
    onChangeColor: (String) -> Unit = { },
    onDelete: (Int) -> Unit = { },
    onReorder: (startIndex: Int, endIndex: Int) -> Unit = { _, _ -> },
) {
    Column(modifier) {
        HeaderBar(
            title = "Accounts",
            backArrow = true,
            onBack = onBack,
            modifier = Modifier.zIndex(2f)
        ) {
            Spacer(Modifier.weight(10f))
            IconButton(onClick = onAddAccount) {
                Icon(Icons.Sharp.Add, null)
            }
        }

        DragHost {
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(accounts) { index, account ->
                    if (index > 0) RowDivider(Modifier.zIndex(1f))

                    DragToReorderTarget(
                        index = index,
                        onDragEnd = { _, start, end -> onReorder(start, end) },
                    ) {
                        ColorCodedRow(
                            color = account.color,
                        ) {
                            Text(account.name, modifier = Modifier.weight(10f))
                            DropdownOptions(options = accountOptions) {
                                when (it) {
                                    AccountOptions.EDIT -> onChangeName(index)
                                    AccountOptions.COLOR -> onChangeColor("account_" + account.name)
                                    AccountOptions.DELETE -> onDelete(index)
                                }
                            }
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