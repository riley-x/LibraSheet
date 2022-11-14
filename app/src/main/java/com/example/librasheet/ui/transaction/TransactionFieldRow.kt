package com.example.librasheet.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.*
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun TransactionFieldRow(
    label: String,
    modifier: Modifier = Modifier,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit = { },
) {
    Row(
        verticalAlignment = alignment,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp)
            .heightIn(min = libraRowHeight)
    ) {
        Text(
            text = label,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.body2,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
            modifier = Modifier
                .padding(end = 15.dp)
                .width(60.dp)
        )
        content()
    }
}


@Composable
fun TransactionEditRow(
    label: String,
    text: String,
    modifier: Modifier = Modifier,
    lines: Int = 1,
    onValueChange: (String) -> Unit = { },
) {
    var enabled by remember { mutableStateOf(false) }

    TransactionFieldRow(
        label = label,
        alignment = if (lines == 1) Alignment.CenterVertically else Alignment.Top,
        modifier = modifier,
    ) {
        if (enabled) {
            BasicTextField(
                value = text,
                onValueChange = onValueChange,
                singleLine = lines == 1,
                textStyle = MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.high)
                ),
                maxLines = lines,
                modifier = Modifier.weight(10f)
            )
        } else { // Need this since BasicTextField doesn't have TextOverflow
            Text(
                text = text,
                maxLines = lines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(10f)
            )
        }
        IconButton(onClick = { enabled = true }) {
            Icon(imageVector = Icons.Sharp.Edit, contentDescription = null)
        }
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

@Preview
@Composable
private fun PreviewEdit() {
    LibraSheetTheme {
        Surface {
            TransactionEditRow(
                label = "Name",
                text = "BANK OF AMERICA CREDIT CARD Bill Payment",
                lines = 3,
            )
        }
    }
}