package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewLineGraph
import com.example.librasheet.viewModel.preview.previewLineGraphAxes
import kotlin.math.roundToInt


/**
 * A line graph has equally spaced values joined by line segments. The user x coordinates in this
 * case are simply 0..lastIndex.
 */
@Composable
fun lineGraphDrawer(
    values: SnapshotStateList<Float>,
    color: Color = MaterialTheme.colors.onSurface,
    size: Float = with(LocalDensity.current) { 2.dp.toPx() },
): DrawScope.(GrapherInputs) -> Unit {
    return fun DrawScope.(grapherInputs: GrapherInputs) {
        fun loc(i: Int) = Offset(
            x = grapherInputs.userToPxX(i.toFloat()),
            y = grapherInputs.userToPxY(values[i])
        )

        val path = Path().apply {
            moveTo(loc(0))
            for (i in 1 until values.size) {
                 lineTo(loc(i))
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = size,
            )
        )
    }
}

@Composable
fun LineGraph(
    state: DiscreteGraphState,
    modifier: Modifier = Modifier,
    onHover: (isHover: Boolean, loc: Int) -> Unit = { _, _ -> },
) {
    val hoverLoc = remember { mutableStateOf(-1) }
    val showHover by remember { derivedStateOf { hoverLoc.value >= 0 } }
    val graph = lineGraphDrawer(values = state.values)
    val graphHover = discreteHover(loc = hoverLoc)
    fun onHoverInner(isHover: Boolean, x: Float, y: Float) {
        if (state.values.isEmpty()) return
        if (isHover) {
            hoverLoc.value = MathUtils.clamp(x.roundToInt(), 0, state.values.lastIndex)
        } else {
            hoverLoc.value = -1
        }
        onHover(isHover, hoverLoc.value)
    }
    Graph(
        axesState = state.axes,
        onHover = ::onHoverInner,
        modifier = modifier,
    ) {
        graph(it)
        if (showHover) graphHover(it)
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Graph(
                axesState = previewLineGraphAxes,
                contentAfter = lineGraphDrawer(previewLineGraph),
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}
