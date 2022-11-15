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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import com.example.librasheet.ui.components.NoDataError
import com.example.librasheet.ui.components.format2Decimals
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewStackedLineGraph
import com.example.librasheet.viewModel.preview.previewStackedLineGraphAxes
import kotlin.math.roundToInt


typealias StackedLineGraphValue = Pair<Color, List<Float>>

@Immutable
data class StackedLineGraphState(
    val axes: MutableState<AxesState> = mutableStateOf(AxesState()),
    val values: SnapshotStateList<StackedLineGraphValue> = mutableStateListOf(),
    val toString: MutableState<(Float) -> String> = mutableStateOf({ "$it" }),
)


/**
 * A stacked line graph draws a fill between each series. The user x coordinates in this
 * case are simply 0..lastIndex.
 *
 * @property values A list of series, in reverse order. The first entry should be the top of the
 * stack, with values pre-added.
 */
@Composable
fun stackedLineGraphDrawer(
    values: SnapshotStateList<StackedLineGraphValue>,
): DrawScope.(GrapherInputs) -> Unit {
    return fun DrawScope.(grapherInputs: GrapherInputs) {
        values.forEach { (color, series) ->
            fun loc(i: Int) = Offset(
                x = grapherInputs.userToPxX(i.toFloat()),
                y = grapherInputs.userToPxY(series[i])
            )

            val path = Path().apply {
                moveTo(grapherInputs.userToPxX(0f), grapherInputs.userToPxY(grapherInputs.axesState.minY))
                series.indices.forEach { lineTo(loc(it)) }
                lineTo(grapherInputs.userToPxX(series.lastIndex.toFloat()), grapherInputs.userToPxY(grapherInputs.axesState.minY))
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

/**
 * When hovering over a stacked line graph, display a vertical line at the hover position. Also,
 * For each series, draw a colored flag above the y-axis labels, at the y-value of the current hover,
 * with the value as a label.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun stackedLineGraphHover(
    values: SnapshotStateList<StackedLineGraphValue>,
    hoverLoc: State<Int>,
    indicatorColor: Color = MaterialTheme.colors.onSurface,
    textDarkColor: Color = MaterialTheme.colors.background,
    indicatorWidth: Dp = 1.dp,
    labelYStartPad: Dp = 8.dp,
    toString: (Float) -> String = { "$it" },
): DrawScope.(GrapherInputs) -> Unit {
    val textStyle = MaterialTheme.typography.overline
    return fun DrawScope.(it: GrapherInputs) {
        /** Indicator line **/
        drawLine(
            color = indicatorColor,
            start = Offset(
                it.userToPxX(hoverLoc.value.toFloat()),
                it.userToPxY(it.axesState.minY)
            ),
            end = Offset(
                it.userToPxX(hoverLoc.value.toFloat()),
                it.userToPxY(it.axesState.maxY)
            ),
            strokeWidth = indicatorWidth.toPx(),
        )

        /** Value flags **/
        val endX = it.userToPxX(it.axesState.maxX)
        val flagPointX = endX + 2f
        val flagBaseX = endX + labelYStartPad.toPx()
        values.forEachIndexed { index, (color, series) ->
            /** Get the label text layout **/
            val value = series[hoverLoc.value] -
                if (index < values.lastIndex)
                    values[index + 1].second[hoverLoc.value]
                else 0f
            val layoutResult = it.textMeasurer.measure(
                text = AnnotatedString(toString(value)),
                style = textStyle,
            )

            /** Calculate the flag path **/
            val labelStartX = size.width - layoutResult.size.width
            val labelCenterY = it.userToPxY(series[hoverLoc.value])
            val labelHalfHeight = layoutResult.size.height / 2
            val labelTop = labelCenterY - labelHalfHeight
            val labelBottom = labelCenterY + labelHalfHeight

            val path = Path().apply {
                moveTo(flagPointX, labelCenterY)
                lineTo(flagBaseX, labelTop)
                lineTo(size.width, labelTop)
                lineTo(size.width, labelBottom)
                lineTo(flagBaseX, labelBottom)
                close()
            }

            /** Draw path and label **/
            drawPath(
                path = path,
                color = color,
                alpha = 1.0f
            )
            drawText(
                textLayoutResult = layoutResult,
                color = if (color.luminance() > 0.5) textDarkColor else indicatorColor,
                topLeft = Offset(
                    x = labelStartX,
                    y = labelCenterY - labelHalfHeight
                )
            )
        }
    }
}



@Composable
fun StackedLineGraph(
    state: StackedLineGraphState,
    modifier: Modifier = Modifier,
    onHover: (isHover: Boolean, loc: Int) -> Unit = { _, _ -> },
) {
    if (state.values.isEmpty()) {
        NoDataError(modifier)
    } else {
        val hoverLoc = remember { mutableStateOf(-1) }
        val showHover by remember { derivedStateOf { hoverLoc.value >= 0 } }
        val graph = stackedLineGraphDrawer(values = state.values)
        val graphHover = stackedLineGraphHover(
            values = state.values,
            hoverLoc = hoverLoc,
            toString = state.toString.value,
        )

        fun onHoverInner(isHover: Boolean, x: Float, y: Float) {
            if (state.values.isEmpty()) return
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
                contentBefore = stackedLineGraphDrawer(previewStackedLineGraph),
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}


@Preview
@Composable
private fun PreviewHover() {
    LibraSheetTheme {
        Surface {
            val hoverLoc = remember { mutableStateOf(2) }
            val graph = stackedLineGraphDrawer(previewStackedLineGraph)
            val graphHover = stackedLineGraphHover(previewStackedLineGraph, hoverLoc) {
                format2Decimals(it / 1000)
            }
            Graph(
                axesState = previewStackedLineGraphAxes,
                contentBefore = graph,
                contentAfter = graphHover,
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}
