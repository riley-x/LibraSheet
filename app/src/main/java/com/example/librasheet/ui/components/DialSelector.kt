package com.example.librasheet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIos
import androidx.compose.material.icons.sharp.ArrowForwardIos
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
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
    selected: State<Int>,
    labels: ImmutableList<String>,
    modifier: Modifier = Modifier,
    onSelection: (Int, wasFromDialRight: Boolean) -> Unit = { _, _ -> },
) {
    fun back() =
        if (selected.value == 0) labels.items.lastIndex
        else selected.value - 1

    fun next() =
        if (selected.value == labels.items.lastIndex) 0
        else selected.value + 1

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
                IconButton(onClick = { onSelection(back(), false) }) {
                    Icon(imageVector = Icons.Sharp.ArrowBackIos, contentDescription = null)
                }

                Spacer(modifier = Modifier.weight(10f))

                Text(
                    text = labels.items[selected.value],
                    style = MaterialTheme.typography.h2,
                )

                Spacer(modifier = Modifier.weight(10f))

                IconButton(onClick = { onSelection(next(), true) }) {
                    Icon(imageVector = Icons.Sharp.ArrowForwardIos, contentDescription = null)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                labels.items.indices.forEach {
                    Canvas(modifier = Modifier.size(7.dp)) {
                        val r = size.minDimension / 2.0f
                        val border = 1.dp.toPx()
                        drawCircle(
                            color = color,
                            radius = if (it == selected.value) r else r - border / 2f,
                            style = if (it == selected.value) Fill else Stroke(width = border)
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
    val selected = remember { mutableStateOf(0) }
    LibraSheetTheme {
        Surface {
            DialSelector(selected = selected, labels = previewGraphLabels)
        }
    }
}