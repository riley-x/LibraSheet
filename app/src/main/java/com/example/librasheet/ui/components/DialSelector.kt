package com.example.librasheet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIos
import androidx.compose.material.icons.sharp.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewGraphLabels


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DialSelector(
    selectedIndex: Int,
    labels: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onSelection: (Int) -> Unit = { },
) {
    fun back() =
        if (selectedIndex == 0) labels.lastIndex
        else selectedIndex - 1
    fun next() =
        if (selectedIndex == labels.lastIndex) 0
        else selectedIndex + 1

    CompositionLocalProvider(
        LocalMinimumTouchTargetEnforcement provides false,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onSelection(back()) }) {
                    Icon(imageVector = Icons.Sharp.ArrowBackIos, contentDescription = null)
                }

                Spacer(modifier = Modifier.weight(10f))

                Text(
                    text = labels[selectedIndex],
                    style = MaterialTheme.typography.h2,
                )

                Spacer(modifier = Modifier.weight(10f))

                IconButton(onClick = { onSelection(next()) }) {
                    Icon(imageVector = Icons.Sharp.ArrowForwardIos, contentDescription = null)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                labels.indices.forEach {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        val r = size.minDimension / 2.0f
                        val border = 2.dp.toPx()
                        drawCircle(
                            color = color,
                            radius = if (it == selectedIndex) r else r - border / 2f,
                            style = if (it == selectedIndex) Fill else Stroke(width = border)
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
    LibraSheetTheme {
        Surface {
            DialSelector(selectedIndex = 0, labels = previewGraphLabels)
        }
    }
}