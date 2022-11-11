package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
internal fun BackupDatabaseCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            CardTitle(title = "Backup Database") {
                IconButton(onClick = onClick) {
                    Icon(imageVector = Icons.Sharp.Share, contentDescription = null)
                }
            }
        }
    }
}


@Preview(
    widthDp = 360,
)
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            BackupDatabaseCard(
            )
        }
    }
}