package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

/** This applies a weight of 10 to the title text **/
@Composable
fun CardTitle(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 10.dp)
            .heightIn(min = 48.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h2,
            modifier = Modifier.weight(10f)
        )

        content()
    }
}


@Preview(widthDp = 360)
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            CardTitle(title = "Card Title")
        }
    }
}