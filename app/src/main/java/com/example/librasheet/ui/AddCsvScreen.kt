package com.example.librasheet.ui

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
import com.example.librasheet.viewModel.preview.previewAccounts

@Composable
fun AddCsvScreen(
    accounts: SnapshotStateList<Account>,
    fileName: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
    loadCsv: () -> Unit = { },
) {
    var account by remember { mutableStateOf<Account?>(null) }
    var invertValues by remember { mutableStateOf(false) }

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
                selection = account,
                options = accounts,
                onSelection = { account = it },
                modifier = Modifier.weight(10f),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text("Invert values:")
            Checkbox(
                checked = invertValues,
                onCheckedChange = { invertValues = it },
            )
        }

        if (fileName.isEmpty()) {
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = loadCsv,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Load CSV", fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null)
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .height(48.dp)
            ) {
                Text("CSV: $fileName")
            }
            
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
                fileName = "",
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
            AddCsvScreen(
                accounts = previewAccounts,
                fileName = "filename.csv",
            )
        }
    }
}