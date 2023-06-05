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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import com.example.librasheet.ui.components.NoDataError
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewNetIncome
import com.example.librasheet.viewModel.preview.previewNetIncomeAxes
import com.example.librasheet.viewModel.preview.previewStackedLineGraph
import com.example.librasheet.viewModel.preview.previewStackedLineGraphAxes
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A stacked bar graph draws a filled bar for each series. It is similar to a StackedLineGraph
 * except the bars are discrete instead of being connected by a path.
 *
 * The user x coordinates in this case are simply 0..lastIndex.
 *
 * @property values A list of series, in reverse order. The first entry should be the top of the
 * stack, with values pre-added. All values should be greater than 0.
 * @property barWidth Width of each bar as a fraction [0, 1] of the distance between them
 */
@Composable
fun stackedBarGraphDrawer(
    values: SnapshotStateList<StackedLineGraphValue>,
    barWidth: Float = 0.9f,
): DrawScope.(GrapherInputs) -> Unit {
    return fun DrawScope.(it: GrapherInputs) {
        val width = barWidth * (it.userToPxX(1f) - it.userToPxX(0f))
        val base = it.userToPxY(0f)

        values.forEach { (color, series) ->
            series.forEachIndexed { index, value ->
                val x = it.userToPxX(index.toFloat())
                val y = it.userToPxY(value.toFloat())
                drawRect(
                    color = color,
                    topLeft = Offset(x - width / 2, y),
                    size = Size(width, base - y),
                )
            }
        }
    }
}


@Composable
fun StackedBarGraph(
    state: StackedLineGraphState,
    modifier: Modifier = Modifier,
    onHover: (isHover: Boolean, loc: Int) -> Unit = { _, _ -> },
) {
    if (state.values.isEmpty() || state.values.first().second.isEmpty()) {
        NoDataError(modifier)
    } else {
        val hoverLoc = remember { mutableStateOf(-1) }
        val showHover by remember { derivedStateOf { hoverLoc.value >= 0 } }
        val graph = stackedBarGraphDrawer(values = state.values)
        val graphHover = stackedLineGraphHover(
            values = state.values,
            hoverLoc = hoverLoc,
            toString = state.toString.value,
        )

        fun onHoverInner(isHover: Boolean, x: Float, y: Float) {
            if (state.values.isEmpty()) return
            if (state.values.first().second.isEmpty()) return
            if (isHover) {
                hoverLoc.value =
                    MathUtils.clamp(x.roundToInt(), 0, state.values.first().second.lastIndex)
            } else {
                hoverLoc.value = -1
            }
            onHover(isHover, hoverLoc.value)
        }
        Graph(
            axesState = state.axes,
            contentBefore = graph,
            contentAfter = { if (showHover) graphHover(it) },
            onHover = ::onHoverInner,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Graph(
                axesState = previewStackedLineGraphAxes,
                contentBefore = stackedBarGraphDrawer(previewStackedLineGraph),
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}

