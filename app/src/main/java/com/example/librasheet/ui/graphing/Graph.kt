package com.example.librasheet.ui.graphing

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
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
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.NamedValue
import com.example.librasheet.viewModel.preview.previewLineGraphAxes

/**
 * Main state class for the graph. When any of these variables changes, the whole graph
 * has to be recomposed.
 *
 * @param ticksX            User x locations of where the x ticks should go, and label
 * @param ticksY            User y locations of where the y ticks should go, and label
 * @param minY              The lower y bound of the graph, in user coordinates
 * @param maxY              The upper y bound of the graph, in user coordinates
 * @param minX              The lower x bound of the graph, in user coordinates
 * @param maxX              The upper x bound of the graph, in user coordinates
 */
@Immutable
data class AxesState(
    val ticksY: List<NamedValue> = emptyList(),
    val ticksX: List<NamedValue> = emptyList(),
    val minY: Float = 0f,
    val maxY: Float = 100f,
    val minX: Float = 0f,
    val maxX: Float = 100f,
)

typealias Grapher = DrawScope.(
    axesState: AxesState,
    userToPxX: (Float) -> Float,
    userToPxY: (Float) -> Float,
) -> Unit

/**
 * This is an "abstract" class that handles the grid, axes, labels, and callbacks of a graph.
 * Users should implement the main graph drawing via the [content] lambda
 *
 * @param axesState         This sets the grid lines and axes labels, and also the x/y axes ranges.
 * @param gridAbove         If true, will plot the grid lines above [content].
 * @param content           The main graphing function. It should call draw functions on the
 *                          passed drawScope parameter.
 * @param hover             Enable hover functionality
 * @param labelXTopPad      Padding above the x tick labels
 * @param labelYStartPad    Padding to the left of the y tick labels
 * @param onPress           Callback on first press down. Can be used to clear focus, for example
 * @param onHover           Callback for when the hover position changes. The x, y parameters are in
 *                          user coordinates. WARNING they can be out of bounds! Make sure to catch.
 */
@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Graph(
    axesState: State<AxesState>,
    modifier: Modifier = Modifier,
    gridAbove: Boolean = false,
    hover: Boolean = true,
    labelYStartPad: Dp = 8.dp, // padding left of label
    labelXTopPad: Dp = 2.dp, // padding top of label
    onHover: (isHover: Boolean, x: Float, y: Float) -> Unit = { _, _, _ -> },
    onPress: () -> Unit = { },
    content: Grapher = { _, _, _ -> },
) {
    /** Cache some text variables (note MaterialTheme is not accessible in DrawScope) **/
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.overline
    val textColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
    val labelYWidth = if (axesState.value.ticksY.isEmpty()) 0 else {
        val textLayoutResult1: TextLayoutResult =
            textMeasurer.measure( // Use the first and last y tick to estimate text extent
                text = AnnotatedString(axesState.value.ticksY.last().name),
                style = textStyle,
            )
        val textLayoutResult2: TextLayoutResult = if (axesState.value.ticksY.size == 1) textLayoutResult1 else
            textMeasurer.measure( // Use the last y tick (~widest value) to estimate text extent
                text = AnnotatedString(axesState.value.ticksY.first().name),
                style = textStyle,
            )
        maxOf(textLayoutResult1.size.width, textLayoutResult2.size.width)
    }
    val labelXHeight = if (axesState.value.ticksX.isEmpty()) 0 else {
        val textLayoutResult: TextLayoutResult =
            textMeasurer.measure( // Use the first x tick cause no better
                text = AnnotatedString(axesState.value.ticksX.first().name),
                style = textStyle,
            )
        textLayoutResult.size.height
    }

    /** Other config vars **/
    val gridColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
    val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val labelYOffsetPx = with(LocalDensity.current) { labelYStartPad.toPx() }
    val labelXOffsetPx = with(LocalDensity.current) { labelXTopPad.toPx() }

    /** Hover. These need to use boxSize instead of DrawScope.size **/
    var boxSize by remember { mutableStateOf(IntSize(0, 0)) }
    val disallowIntercept = RequestDisallowInterceptTouchEvent()
    fun pxToUserX(pxX: Float): Float {
        val startX = 0
        val endX = boxSize.width - labelYWidth - labelYOffsetPx
        val deltaX = (endX - startX) / (axesState.value.maxX - axesState.value.minX)
        return axesState.value.minX + (pxX - startX) / deltaX
    }
    fun pxToUserY(pxY: Float): Float {
        val startY = boxSize.height - labelXHeight - labelXOffsetPx
        val endY = 0
        val deltaY = (endY - startY) / (axesState.value.maxY - axesState.value.minY)
        return axesState.value.minY + (pxY - startY) / deltaY
    }
    fun Modifier.hover() = this
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
                val userX = pxToUserX(motionEvent.x)
                val userY = pxToUserY(motionEvent.y)
                onHover(true, userX, userY)
            } else {
                disallowIntercept(false)
                onHover(false, 0f, 0f)
            }
            true
        }

    /** Main canvas **/
    Canvas(modifier = if (hover) modifier.hover() else modifier,
    ) {
        /** User -> pixel conversions **/
        val startX = 0
        val endX = size.width - labelYWidth - labelYOffsetPx
        val deltaX = (endX - startX) / (axesState.value.maxX - axesState.value.minX)

        val startY = size.height - labelXHeight - labelXOffsetPx
        val endY = 0
        val deltaY = (endY - startY) / (axesState.value.maxY - axesState.value.minY)

        fun userToPxX(userX: Float) = startX + deltaX * (userX - axesState.value.minX)
        fun userToPxY(userY: Float) = startY + deltaY * (userY - axesState.value.minY)

        /** Main Plot **/
        if (gridAbove) content(axesState.value, ::userToPxX, ::userToPxY)

        /** Y Gridlines and Axis Labels **/
        for (tick in axesState.value.ticksY) {
            val y = startY + deltaY * (tick.value - axesState.value.minY)
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
        for (tick in axesState.value.ticksX) {
            val x = (tick.value - axesState.value.minX) * deltaX
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

        /** Main Plot **/
        if (!gridAbove) content(axesState.value, ::userToPxX, ::userToPxY)
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Graph(
                axesState = previewLineGraphAxes,
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}