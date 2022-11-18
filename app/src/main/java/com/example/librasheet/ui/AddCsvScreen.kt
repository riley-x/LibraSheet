package com.example.librasheet.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.librasheet.data.entity.Account
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.RowTitle
import com.example.librasheet.ui.components.selectors.AccountSelector
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.CsvModel
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewCsvModel

@Composable
fun AddCsvScreen(
    state: CsvModel,
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    loadCsv: (Uri?) -> Unit = { },
) {
    /** https://developer.android.com/jetpack/compose/libraries#activity_result **/
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        loadCsv(it)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HeaderBar(
            title = "Add CSV",
            backArrow = true,
            onBack = onBack,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text("Account:")
            Spacer(Modifier.width(15.dp))
            AccountSelector(
                selection = state.account,
                options = accounts,
                onSelection = state::setAcc,
                modifier = Modifier.weight(10f),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .height(48.dp)
        ) {
            Text("Pattern:")
            Spacer(Modifier.width(15.dp))
            OutlinedTextField(
                value = state.pattern,
                onValueChange = state::setPatt,
                modifier = Modifier.weight(10f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .height(48.dp)
        ) {
            Text("Date format:")
            Spacer(Modifier.width(15.dp))
            OutlinedTextField(
                value = state.dateFormat,
                onValueChange = state::setDateForm,
                modifier = Modifier.weight(10f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text("Invert values:")
            Checkbox(
                checked = state.invertValues,
                onCheckedChange = state::setInvert,
            )
        }

        if (state.transactions.isEmpty()) {
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { launcher.launch(arrayOf("text/*")) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Load CSV", fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null)
            }
        } else {

            
            RowTitle(title = "Preview Transactions:")

            LazyColumn(
                modifier = Modifier.weight(10f)
            ) {

            }
        }
    }
}


@Preview(
    heightDp = 300
)
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            AddCsvScreen(
                accounts = previewAccounts,
                state = previewCsvModel,
            )
        }
    }
}


@Preview(
    heightDp = 400
)
@Composable
private fun PreviewLoaded() {
    LibraSheetTheme {
        Surface {
//            AddCsvScreen(
//                accounts = previewAccounts,
//                fileName = "filename.csv",
//            )
        }
    }
}