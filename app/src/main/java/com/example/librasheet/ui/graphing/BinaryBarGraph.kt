package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewNetIncome
import com.example.librasheet.viewModel.preview.previewNetIncomeAxes
import com.example.librasheet.viewModel.preview.previewStackedLineGraph
import com.example.librasheet.viewModel.preview.previewStackedLineGraphAxes
import kotlin.math.abs

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
    return fun DrawScope.(grapherInputs: GrapherInputs) {
        val width = barWidth * (grapherInputs.userToPxX(1f) - grapherInputs.userToPxX(0f))
        val axis = grapherInputs.userToPxY(xAxisLoc)

        values.forEachIndexed { index, value ->
            val x = grapherInputs.userToPxX(index.toFloat())
            val y = grapherInputs.userToPxY(value)
            drawRect(
                color = if (value > xAxisLoc) aboveColor else belowColor,
                topLeft = Offset(x - width / 2, minOf(y, axis)),
                size = Size(width, abs(y - axis)),
            )
        }

        drawLine(
            color = axisColor,
            start = Offset(grapherInputs.userToPxX(grapherInputs.axesState.minX), axis),
            end = Offset(grapherInputs.userToPxX(grapherInputs.axesState.maxX), axis),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Graph(
                axesState = previewNetIncomeAxes,
                contentAfter = binaryBarGraphDrawer(previewNetIncome),
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}
