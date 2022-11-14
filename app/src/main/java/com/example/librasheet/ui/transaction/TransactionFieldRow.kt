package com.example.librasheet.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.TransactionEntity
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.components.formatDateInt
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.components.libraRow
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewTransactions

@Composable
fun TransactionFieldRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = { },
) {
    Row(
        modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.body2,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
            modifier = Modifier.width(100.dp)
        )
        content()
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionFieldRow(
                label = "Category"
            )
        }
    }
}