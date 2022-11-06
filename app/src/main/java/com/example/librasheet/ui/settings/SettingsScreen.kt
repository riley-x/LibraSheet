package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackupDatabase: () -> Unit = { },
) {
    // TODO: Maybe this screen is a good place for dividend and option summaries

    Column(modifier) {
        HeaderBar(title = "Settings")

        LazyColumn {

            item(key = "backup database") {
                BackupDatabaseCard(
                    onClick = onBackupDatabase,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
    showBackground = true,
)
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            SettingsScreen(

            )
        }
    }
}