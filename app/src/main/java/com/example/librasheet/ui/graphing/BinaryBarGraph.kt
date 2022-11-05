package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewNetIncome
import com.example.librasheet.viewModel.preview.previewNetIncomeAxes
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A bar graph that draws the fill color based on whether the value is above or below a threshold.
 * The user x coordinates in this case are simply 0..lastIndex.
 *
 * @property xAxisLoc Base point of where the bars are drawn, and also the threshold that determines
 *      the fill color, in user coordinates.
 * @property aboveColor Color of the fill for values above [xAxisLoc]
 * @property belowColor Color of the fill for values below [xAxisLoc]
 * @property barWidth Width of each bar as a fraction [0, 1] of the distance between them
 */
@Composable
fun binaryBarGraphDrawer(
    values: SnapshotStateList<Float>,
    xAxisLoc: Float = 0f,
    aboveColor: Color = MaterialTheme.colors.primary,
    belowColor: Color = MaterialTheme.colors.error,
    axisColor: Color = MaterialTheme.colors.onSurface,
    barWidth: Float = 0.9f,
): DrawScope.(GrapherInputs) -> Unit {
    return fun DrawScope.(it: GrapherInputs) {
        val width = barWidth * (it.userToPxX(1f) - it.userToPxX(0f))
        val axis = it.userToPxY(xAxisLoc)

        values.forEachIndexed { index, value ->
            val x = it.userToPxX(index.toFloat())
            val y = it.userToPxY(value)
            drawRect(
                color = if (value > xAxisLoc) aboveColor else belowColor,
                topLeft = Offset(x - width / 2, minOf(y, axis)),
                size = Size(width, abs(y - axis)),
            )
        }

        drawLine(
            color = axisColor,
            start = Offset(it.userToPxX(it.axesState.minX), axis),
            end = Offset(it.userToPxX(it.axesState.maxX), axis),
            strokeWidth = 1.dp.toPx(),
        )
    }
}


@Composable
fun BinaryBarGraph(
    axes: State<AxesState>,
    values: SnapshotStateList<Float>,
    modifier: Modifier = Modifier,
    onHover: (isHover: Boolean, loc: Int) -> Unit = { _, _ -> },
) {
    val hoverLoc = remember { mutableStateOf(-1) }
    val showHover by remember { derivedStateOf { hoverLoc.value >= 0 } }
    val graph = binaryBarGraphDrawer(values = values)
    val graphHover = discreteHover(loc = hoverLoc)
    fun onHoverInner(isHover: Boolean, x: Float, y: Float) {
        if (values.isEmpty()) return
        if (isHover) {
            hoverLoc.value = MathUtils.clamp(x.roundToInt(), 0, values.lastIndex)
        } else {
            hoverLoc.value = -1
        }
        onHover(isHover, hoverLoc.value)
    }
    Graph(
        axesState = axes,
        contentBefore = graph,
        contentAfter = { if (showHover) graphHover(it) },
        onHover = ::onHoverInner,
        modifier = modifier,
    )
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Graph(
                axesState = previewNetIncomeAxes,
                contentBefore = binaryBarGraphDrawer(previewNetIncome),
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}
