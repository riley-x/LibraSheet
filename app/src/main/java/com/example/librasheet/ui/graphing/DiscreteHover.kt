package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewStackedLineGraphAxes

/**
 * Draws a vertical line at the hover position, used for discrete graphs where the user x coordinates
 * are simply the indices into a data array.
 */
@Composable
fun discreteHover(
    hoverLoc: State<Int>,
    width: Dp = 1.dp,
    color: Color = MaterialTheme.colors.onSurface,
): DrawScope.(GrapherInputs) -> Unit {
    return fun DrawScope.(it: GrapherInputs) {
        drawLine(
            color = color,
            start = Offset(
                it.userToPxX(hoverLoc.value.toFloat()),
                it.userToPxY(it.axesState.minY)
            ),
            end = Offset(
                it.userToPxX(hoverLoc.value.toFloat()),
                it.userToPxY(it.axesState.maxY)
            ),
            strokeWidth = width.toPx()
        )
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            val hoverLoc = remember { mutableStateOf(3) }
            val graphHover = discreteHover(hoverLoc)
            Graph(
                axesState = previewStackedLineGraphAxes,
                contentAfter = graphHover,
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}