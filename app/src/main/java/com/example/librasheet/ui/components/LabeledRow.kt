package com.example.librasheet.ui.components

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
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun LabeledRow(
    label: String,
    modifier: Modifier = Modifier,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit = { },
) {
    Row(
        verticalAlignment = alignment,
        modifier = modifier
    ) {
        Text(
            text = label,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.body2,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp)
                .padding(end = 15.dp)
                .width(80.dp)
        )
        content()
    }
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            LabeledRow(
                label = "Category"
            )
        }
    }
}