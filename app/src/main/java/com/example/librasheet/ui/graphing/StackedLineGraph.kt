package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewStackedLineGraph
import com.example.librasheet.viewModel.preview.previewStackedLineGraphAxes


typealias StackedLineGraphValues = List<Pair<Color, List<Float>>>

/**
 * A stacked line graph draws a fill between each series. The user x coordinates in this
 * case are simply 0..lastIndex.
 *
 * @property values A list of series, in reverse order. The first entry should be the top of the
 * stack, with values pre-added.
 */
@Composable
fun stackedLineGraph(
    values: State<StackedLineGraphValues>,
): Grapher {
    return fun DrawScope.(
        axesState: AxesState,
        userToPxX: (Float) -> Float,
        userToPxY: (Float) -> Float,
    ) {
        values.value.forEach { (color, series) ->
            fun loc(i: Int) = Offset(
                x = userToPxX(i.toFloat()),
                y = userToPxY(series[i])
            )

            val path = Path().apply {
                moveTo(userToPxX(0f), userToPxY(axesState.minY))
                series.indices.forEach { lineTo(loc(it)) }
                lineTo(userToPxX(series.lastIndex.toFloat()), userToPxY(axesState.minY))
                close()
            }

            drawPath(
                path = path,
                color = color,
                style = Fill,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Graph(
                axesState = previewStackedLineGraphAxes,
                content = stackedLineGraph(previewStackedLineGraph),
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}
