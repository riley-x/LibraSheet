package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun ConfirmationButtons(
    modifier: Modifier = Modifier,
    cancelText: String = "Cancel",
    okText: String = "Ok",
    onCancel: () -> Unit = { },
    onOk: () -> Unit = { },
) {
    Row(
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.error,
            )
        ) {
            Text(cancelText)
        }

        Button(
            onClick = onOk,
        ) {
            Text(okText)
        }
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        ConfirmationButtons()
    }
}