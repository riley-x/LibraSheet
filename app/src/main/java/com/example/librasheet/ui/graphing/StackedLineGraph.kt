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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.format2Decimals
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


@OptIn(ExperimentalTextApi::class)
@Composable
fun stackedLineGraphHover(
    values: State<StackedLineGraphValues>,
    hoverLoc: State<Int>,
    crosshairColor: Color = MaterialTheme.colors.onSurface,
    labelYStartPad: Dp = 8.dp,
): DrawScope.(GrapherInputs) -> Unit {
    val textStyle = MaterialTheme.typography.overline
    return fun DrawScope.(it: GrapherInputs) {
        /** Crosshairs **/
        drawLine(
            color = crosshairColor,
            start = Offset(
                it.userToPxX(hoverLoc.value.toFloat()),
                it.userToPxY(it.axesState.minY)
            ),
            end = Offset(
                it.userToPxX(hoverLoc.value.toFloat()),
                it.userToPxY(it.axesState.maxY)
            ),
        )

        /** Value flags. For each series, draw a colored flag at the value of the current hover,
         * with the value as a label. **/
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
                color = crosshairColor,
                topLeft = Offset(
                    x = labelStartX,
                    y = labelCenterY - labelHalfHeight
                )
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
                contentBefore = stackedLineGraph(previewStackedLineGraph),
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
            val graph = stackedLineGraph(previewStackedLineGraph)
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
