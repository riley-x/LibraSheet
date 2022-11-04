package com.example.librasheet.ui.graphing

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils.clamp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.NamedValue
import com.example.librasheet.viewModel.preview.previewBalanceHistoryState
import kotlin.math.roundToInt


/**
 * Where
 *      pixX = deltaX * (userX - minX)
 *      pixY = startY + deltaY * (userY - minY)
 */
typealias TimeSeriesGrapher<T> = (
    drawScope: DrawScope,
    values: List<T>,
    deltaX: Float,
    deltaY: Float,
    startY: Float,
    minX: Float,
    minY: Float
) -> Unit


/**
 * Main state class for the time series graph. When any of these variables changes, the whole graph
 * has to be recomposed.
 *
 * @param ticksX            User x indices (0..n-1) of which values to use as the x ticks
 * @param ticksY            User y locations of where the y ticks should go, and label
 * @param minY              The lower y bound of the graph, in user coordinates
 * @param maxY              The upper y bound of the graph, in user coordinates
 * @param padX              The amount of padding at each left/right, as fraction of distance between points
 * @param xAxisLoc          User y location to draw the x axis, or null for no axis
 */
@Immutable
data class TimeSeriesGraphState<T>(
    val values: List<T> = emptyList(),
    val ticksY: List<NamedValue> = emptyList(),
    val ticksX: List<NamedValue> = emptyList(),
    val minY: Float = 0f,
    val maxY: Float = 100f,
    val padX: Float = 0f,
    val xAxisLoc: Float? = null, // y value of x axis location
)

/**
 * Plots a graph where the x axis has discrete values. This is an "abstract" class that handles the
 * grid, axes, labels, hover, and callbacks. Users should implement the main graph drawing with one
 * of the following in the grapher argument:
 *      TimeSeriesLineGraph
 *      TimeSeriesCandlestickGraph
 *
 * TODO maybe make hover a separate canvas? So don't recompose the main graph every touch
 *
 * @param grapher           The main graphing function. It should call draw functions on the
 *                          passed drawScope parameter.
 * @param labelXTopPad      Padding above the x tick labels
 * @param labelYStartPad    Padding to the left of the y tick labels
 * @param onPress           Callback on first press down. Can be used to clear focus, for example
 * @param onHover           Callback for when the hover position changes. Returns the original index
 *                          into values for x and the dollar value for y. WARNING x, y can be out of
 *                          bounds! Make sure to catch.
 */
@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T> TimeSeriesGraph(
    state: State<TimeSeriesGraphState<T>>,
    modifier: Modifier = Modifier,
    labelYStartPad: Dp = 8.dp, // padding left of label
    labelXTopPad: Dp = 2.dp, // padding top of label
    grapher: TimeSeriesGrapher<T> = { _, _, _, _, _, _, _ -> },
    onHover: (isHover: Boolean, x: Int, y: Float) -> Unit = { _, _, _ -> },
    onPress: () -> Unit = { },
) {
    if (state.value.values.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            Text(
                text = "No data!",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.error,
            )
        }
    }
    else {
        /** Cache some text variables (note MaterialTheme is not accessible in DrawScope) **/
        val textMeasurer = rememberTextMeasurer()
        val textStyle = MaterialTheme.typography.overline
        val textColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
        val labelYWidth = if (state.value.ticksY.isEmpty()) 0 else {
            val textLayoutResult1: TextLayoutResult =
                textMeasurer.measure( // Use the first and last y tick to estimate text extent
                    text = AnnotatedString(state.value.ticksY.last().name),
                    style = textStyle,
                )
            val textLayoutResult2: TextLayoutResult = if (state.value.ticksY.size == 1) textLayoutResult1 else
                textMeasurer.measure( // Use the last y tick (~widest value) to estimate text extent
                    text = AnnotatedString(state.value.ticksY.first().name),
                    style = textStyle,
                )
            maxOf(textLayoutResult1.size.width, textLayoutResult2.size.width)
        }
        val labelXHeight = if (state.value.ticksX.isEmpty()) 0 else {
            val textLayoutResult: TextLayoutResult =
                textMeasurer.measure( // Use the first x tick cause no better
                    text = AnnotatedString(state.value.ticksX.first().name),
                    style = textStyle,
                )
            textLayoutResult.size.height
        }

        /** Other Config Vars **/
        val hoverColor = MaterialTheme.colors.onSurface
        val gridColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
        val axisColor = MaterialTheme.colors.primary
        val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        val axisPathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 20f), 0f)
        val labelYOffsetPx = with(LocalDensity.current) { labelYStartPad.toPx() }
        val labelXOffsetPx = with(LocalDensity.current) { labelXTopPad.toPx() }

        /** Hover vars **/
        var boxSize by remember { mutableStateOf(IntSize(960, 960)) } // 960 x 1644
        val disallowIntercept = RequestDisallowInterceptTouchEvent()
        var hoverXUser by remember { mutableStateOf(-1) }
        var hoverYPx by remember { mutableStateOf(-1f) }

        /** User -> pixel conversions
         *
         * User y coordinates are just the y values. However user x coordinates are 0..n-1, given by
         * xRange. Note however that onHover returns the unsliced index.
         *
         *      pixelX = 0 + deltaX * (index - minX)
         *      pixelY = startY + deltaY * (valueY - minY)
         */
        val endX = boxSize.width - labelYWidth - labelYOffsetPx
        val minX = -state.value.padX
        val maxX = state.value.values.lastIndex + state.value.padX
        val deltaX = endX / (maxX - minX)
        val startY = boxSize.height - labelXHeight - labelXOffsetPx
        val deltaY = -startY / (state.value.maxY - state.value.minY)

        /** Note this doesn't recompose when changing the hover **/
        Canvas(modifier = modifier
            .onGloballyPositioned { boxSize = it.size }
            .pointerInteropFilter(
                requestDisallowInterceptTouchEvent = disallowIntercept
            ) { motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    onPress()
                }
                if (
                    motionEvent.action == MotionEvent.ACTION_MOVE ||
                    motionEvent.action == MotionEvent.ACTION_DOWN
                ) {
                    disallowIntercept(true)
                    val userX = clamp(
                        (motionEvent.x / deltaX + minX).roundToInt(),
                        0,
                        state.value.values.lastIndex
                    )
                    val userY = (motionEvent.y - startY) / deltaY + state.value.minY
                    hoverXUser = userX
                    hoverYPx = motionEvent.y
                    onHover(true, userX, userY)
                } else {
                    disallowIntercept(false)
                    onHover(false, 0, 0f)
                    hoverXUser = -1
                    hoverYPx = -1f
                }
                true
            },
        ) {
            /** Y Gridlines and Axis Labels **/
            for (tick in state.value.ticksY) {
                val y = startY + deltaY * (tick.value - state.value.minY)
                drawLine(
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = endX, y = y),
                    color = gridColor,
                    pathEffect = gridPathEffect,
                )
                val layoutResult: TextLayoutResult =
                    textMeasurer.measure(
                        text = AnnotatedString(tick.name),
                        style = textStyle,
                    )
                drawText(
                    textLayoutResult = layoutResult,
                    color = textColor,
                    topLeft = Offset(
                        x = size.width - layoutResult.size.width,
                        y = y - layoutResult.size.height / 2
                    )
                )
            }

            /** X Gridlines and Axis Labels **/
            for (tick in state.value.ticksX) {
                val x = (tick.value - minX) * deltaX
                drawLine(
                    start = Offset(x = x, y = startY),
                    end = Offset(x = x, y = 0f),
                    color = gridColor,
                    pathEffect = gridPathEffect,
                )
                val layoutResult: TextLayoutResult =
                    textMeasurer.measure(
                        text = AnnotatedString(tick.name),
                        style = textStyle,
                    )
                drawText(
                    textLayoutResult = layoutResult,
                    color = textColor,
                    topLeft = Offset(
                        x = x - layoutResult.size.width / 2,
                        y = size.height - layoutResult.size.height
                    )
                )
            }

            /** X Axis **/
            if (state.value.xAxisLoc != null) {
                val y = startY + deltaY * (state.value.xAxisLoc!! - state.value.minY)
                drawLine(
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = endX, y = y),
                    color = axisColor,
                    pathEffect = axisPathEffect,
                    strokeWidth = 2f,
                )
            }

            /** Main Plot **/
            grapher(this, state.value.values, deltaX, deltaY, startY, minX, state.value.minY)

            /** Hover **/
            if (hoverYPx > 0 && hoverYPx < startY) {
                drawLine(
                    start = Offset(x = 0f, y = hoverYPx),
                    end = Offset(x = endX, y = hoverYPx),
                    color = hoverColor,
                )
            }
            if (hoverXUser >= 0 && hoverXUser < state.value.values.size) {
                // check xRange instead of min/maxX since we don't want to hover over an empty point
                // since hoverXUser is clamped above, could just check if >= 0
                val x = (hoverXUser.toFloat() - minX) * deltaX
                drawLine(
                    start = Offset(x = x, y = startY),
                    end = Offset(x = x, y = 0f),
                    color = hoverColor,
                )
            }
        }
    }
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TimeSeriesGraph(
                state = previewBalanceHistoryState,
                grapher = lineGraph(),
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}
