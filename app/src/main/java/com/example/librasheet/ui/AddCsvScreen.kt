package com.example.librasheet.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.librasheet.data.entity.Account
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.LabeledRow
import com.example.librasheet.ui.components.RowTitle
import com.example.librasheet.ui.components.SwipeToDelete
import com.example.librasheet.ui.components.selectors.AccountSelector
import com.example.librasheet.ui.components.textFields.CustomTextField
import com.example.librasheet.ui.components.textFields.OutlinedTextFieldNoPadding
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.ui.transaction.TransactionRow
import com.example.librasheet.viewModel.CsvModel
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewCsvModel
import com.example.librasheet.viewModel.preview.previewCsvModel2

@Composable
fun AddCsvScreen(
    state: CsvModel,
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    onSave: () -> Unit = { },
) {
    /** https://developer.android.com/jetpack/compose/libraries#activity_result **/
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        state.loadCsv(it)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HeaderBar(
            title = "Add CSV",
            backArrow = true,
            onBack = onBack,
        )

        LazyColumn(
            modifier = Modifier.weight(10f)
        ) {
            item("Account") {
                LabeledRow(label = "Account") {
                    AccountSelector(
                        selection = state.account,
                        options = accounts,
                        onSelection = state::setAcc,
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )
                }
            }

            item("Pattern") {
                LabeledRow(
                    label = "Pattern",
                    modifier = Modifier.padding(end = 12.dp, bottom = 6.dp)
                ) {
                    OutlinedTextFieldNoPadding(
                        value = state.pattern,
                        onValueChange = state::setPatt,
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 6.dp),
                        modifier = Modifier.weight(10f)
                    )
                }
            }

            item("Date Format") {
                LabeledRow(
                    label = "Date Format",
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    OutlinedTextFieldNoPadding(
                        value = state.dateFormat,
                        onValueChange = state::setDateForm,
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 6.dp),
                        modifier = Modifier.weight(10f)
                    )
                }
            }

            item("Invert Values") {
                LabeledRow(
                    label = "Invert Values",
                    modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
                ) {
                    Checkbox(
                        checked = state.invertValues,
                        onCheckedChange = state::setInvert,
                    )
                }
            }

            if (state.transactions.isEmpty()) {
                item("Button") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            onClick = { launcher.launch(arrayOf("text/*")) },
                        ) {
                            Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Load CSV", fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null)
                        }

                        if (state.errorMessage.isNotEmpty()) {
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colors.error,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            } else {
                item("Preview Transactions") {
                    RowTitle(title = "Preview Transactions")
                }
            }


            itemsIndexed(state.transactions) { i, transaction ->
                SwipeToDelete(
                    onDelete = { state.deleteTransaction(i) },
                ) {
                    TransactionRow(transaction = transaction)
                }
            }
        }

        if (state.transactions.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(bottom= 4.dp),
            ) {
                Button(
                    onClick = state::clear,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error,
                    ),
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("Clear", fontSize = 18.sp)
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.width(80.dp)
                ) {
                    Text("Save", fontSize = 18.sp)
                }
            }
        }
    }
}


@Preview(
    heightDp = 400
)
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            AddCsvScreen(
                accounts = previewAccounts,
                state = previewCsvModel2,
            )
        }
    }
}


@Preview
@Composable
private fun PreviewLoaded() {
    LibraSheetTheme {
        Surface {
            AddCsvScreen(
                accounts = previewAccounts,
                state = previewCsvModel,
            )
        }
    }
}