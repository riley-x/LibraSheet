package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import com.example.librasheet.ui.components.format2Decimals
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewStackedLineGraph
import com.example.librasheet.viewModel.preview.previewStackedLineGraphAxes
import kotlin.math.roundToInt


typealias StackedLineGraphValues = List<Pair<Color, List<Float>>>

/**
 * A stacked line graph draws a fill between each series. The user x coordinates in this
 * case are simply 0..lastIndex.
 *
 * @property values A list of series, in reverse order. The first entry should be the top of the
 * stack, with values pre-added.
 */
@Composable
fun stackedLineGraphDrawer(
    values: State<StackedLineGraphValues>,
): DrawScope.(GrapherInputs) -> Unit {
    return fun DrawScope.(grapherInputs: GrapherInputs) {
        values.value.forEach { (color, series) ->
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
    values: State<StackedLineGraphValues>,
    hoverLoc: State<Int>,
    indicatorColor: Color = MaterialTheme.colors.onSurface,
    labelYStartPad: Dp = 8.dp,
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
        )

        /** Value flags **/
        val endX = it.userToPxX(it.axesState.maxX)
        val flagPointX = endX + 2f
        val flagBaseX = endX + labelYStartPad.toPx()
        values.value.forEachIndexed { index, (color, series) ->
            /** Get the label text layout **/
            val value = series[hoverLoc.value] -
                if (index < values.value.lastIndex)
                    values.value[index + 1].second[hoverLoc.value]
                else 0f
            val layoutResult = it.textMeasurer.measure(
                text = AnnotatedString(
                    format2Decimals(value / 1000f)
                ),
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
                color = indicatorColor,
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
    axes: State<AxesState>,
    history: State<StackedLineGraphValues>,
    modifier: Modifier = Modifier,
    onHover: (isHover: Boolean, loc: Int) -> Unit = { _, _ -> },
) {
    val hoverLoc = remember { mutableStateOf(-1) }
    val showHover by remember { derivedStateOf { hoverLoc.value >= 0 } }
    val graph = stackedLineGraphDrawer(values = history)
    val graphHover = stackedLineGraphHover(values = history, hoverLoc = hoverLoc)
    fun onHoverInner(isHover: Boolean, x: Float, y: Float) {
        if (history.value.isEmpty()) return
        if (isHover) {
            hoverLoc.value = MathUtils.clamp(x.roundToInt(), 0, history.value.first().second.lastIndex)
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
            val graphHover = stackedLineGraphHover(previewStackedLineGraph, hoverLoc)
            Graph(
                axesState = previewStackedLineGraphAxes,
                contentBefore = graph,
                contentAfter = graphHover,
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}
