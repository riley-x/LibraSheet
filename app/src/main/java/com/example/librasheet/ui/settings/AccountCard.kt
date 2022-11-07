package com.example.librasheet.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.preview.previewAccounts

@Composable
fun AccountCard(
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier,
    onAddAccount: () -> Unit = { },
    onChangeName: (String) -> Unit = { },
    onChangeColor: (String) -> Unit = { },
    onDelete: (String) -> Unit = { },
    onSeeAllAccounts: () -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            CardTitle(title = "Accounts") {
                IconButton(onClick = onAddAccount) {
                    Icon(
                        imageVector = Icons.Sharp.Add,
                        contentDescription = null,
                    )
                }
            }

            CardRowDivider(color = MaterialTheme.colors.primary)

            accounts.asReversed().take(if (accounts.size > 4) 3 else 4).forEachIndexed { i, account ->
                if (i > 0) CardRowDivider()

                ColorCodedRow(
                    color = account.color,
                    horizontalPadding = cardRowHorizontalPadding,
                ) {
                    Text(account.name, modifier = Modifier.weight(10f))

                    var expanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier
                        .wrapContentSize(Alignment.TopStart)
                    ) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(onClick = { onChangeName(account.name) }) {
                                Text("Rename")
                            }
                            DropdownMenuItem(onClick = { onChangeColor("account_${account.name}") }) {
                                Text("Change Color")
                            }
                            DropdownMenuItem(onClick = { onDelete(account.name) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }

            if (accounts.size > 4) {
                CardRowDivider()
                TextButton(
                    onClick = onSeeAllAccounts,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text("SEE ALL")
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
            AccountCard(accounts = previewAccounts)
        }
    }
}