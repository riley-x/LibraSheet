package com.example.librasheet.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.Account
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.components.LabeledRow
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.CsvModel
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewCsvModel2

@Composable
fun BadCsvScreen(
    lines: SnapshotStateList<Pair<Int, String>>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { },
) {
    Column(modifier) {
        HeaderBar(title = "Bad Lines", backArrow = true, onBack = onBack)

        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            LazyColumn {
                items(lines) {
                    LabeledRow(
                        label = it.first.toString(),
                        labelWidth = 30.dp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = it.second,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
            }
        }
    }
}



@Preview
@Composable
private fun Preview() {
    val badLines = remember {
        mutableStateListOf(
            Pair(1, "There is no problem with placing a horizontal scroll view inside a vertical one: scrolling will work without problems, and the current scrolling direction of the gesture will be chosen based on the first dragged pixels direction."),
            Pair(999, "I've created a fairly classic collapsing image layout in Jetpack compose, where I have an image at the top of the screen which parallax scrolls away and at a certain point I change the toolbar background from transparent to primarySurface. This is all working quite nicely.")
        )
    }
    LibraSheetTheme {
        Surface {
            BadCsvScreen(
                lines = badLines,
            )
        }
    }
}
