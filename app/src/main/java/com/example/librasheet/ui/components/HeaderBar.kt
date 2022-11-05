package com.example.librasheet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun HeaderBar(
    title: String,
    modifier: Modifier = Modifier,
    backArrow: Boolean = false,
    onBack: () -> Unit = { },
    content: @Composable RowScope.() -> Unit = { },
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            if (backArrow) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Sharp.ArrowBack,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Spacer(modifier = Modifier.width(15.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.h1,
            )

            content()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        HeaderBar(title = "Balances")
    }
}

@Preview
@Composable
private fun PreviewBack() {
    LibraSheetTheme {
        HeaderBar(title = "Bank of America", backArrow = true)
    }
}