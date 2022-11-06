package com.example.librasheet.ui.colorSelector

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import com.example.librasheet.ui.theme.LibraSheetTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorSelector(
    currentColor: PolarColor,
    brightness: Float,
    modifier: Modifier = Modifier,
    onPress: () -> Unit = { },
    onChange: (r: Float, phi: Float) -> Unit = { _, _ ->  },
) {
    var boxSize by remember { mutableStateOf(IntSize(820, 820)) } // 960 x 1644

    /** Indicator config **/
    val indicatorRadius = with(LocalDensity.current) { 10.dp.toPx() }
    val indicatorWidth = with(LocalDensity.current) { 5.dp.toPx() }
    val indicatorColor = remember { derivedStateOf {
        if (brightness > 0.5) Color.Black else Color.White
    } }

    /** Conversions between pixel coordinates and unit circle **/
    fun toPolar(point: Offset, clamp: Boolean = false): Pair<Float, Float> {
        val x = point.x - boxSize.width / 2
        val y = point.y - boxSize.height / 2
        var r = 2 * sqrt(x * x + y * y) / minOf(boxSize.width, boxSize.height)
        if (clamp) r = MathUtils.clamp(r, 0f, 1f)

        var phi = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
        if (phi < 0) phi += 360
        return Pair(r, phi)
    }
    fun fromPolar(r: Float, phi: Float): Offset {
        val minSize = minOf(boxSize.width, boxSize.height) / 2f
        val phiRad = Math.toRadians(phi.toDouble()).toFloat()
        val x = r * cos(phiRad) * minSize + boxSize.width / 2f
        val y = r * sin(phiRad) * minSize + boxSize.height / 2f
        return Offset(x, y)
    }
    fun fromPolar(rPhi: Pair<Float, Float>): Offset = fromPolar(rPhi.first, rPhi.second)

    /** Conversions between pixel coordinates and color **/
    fun colorToPixel(color: Color): Offset {
        val polar = colorToPolar(color, brightness)
        return fromPolar(polar.first, polar.second)
    }
    fun pixelToColor(point: Offset): Color {
        val (r,phi) = toPolar(point, clamp = true)
        return colorFromPolar(r, phi, brightness)
    }

    /** Selection state
     * The circle indicator should be moved by dragging instead of tapping. Cache the last position
     * from ACTION_MOVE or ACTION_DOWN **/
    val currentSelection = fromPolar(currentColor.r, currentColor.phi)
    var dragStart by remember { mutableStateOf(Offset(0f, 0f)) }

    /** Main draw **/
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(start = 30.dp, end = 30.dp)
            .aspectRatio(1f)
            .fillMaxWidth()
            .onGloballyPositioned {
                boxSize = it.size
            }
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dragStart = Offset(motionEvent.x, motionEvent.y)
                        onPress()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newPos =
                            currentSelection + Offset(motionEvent.x, motionEvent.y) - dragStart
                        val (r, phi) = toPolar(newPos)
                        dragStart = Offset(motionEvent.x, motionEvent.y)
                        onChange(minOf(r, 1f), phi)
                    }
                }
                true
            }
    ) {
        ColorCircle(brightness = brightness)
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = currentColor.color,
                radius = indicatorRadius,
                center = currentSelection,
            )
            drawCircle(
                color = indicatorColor.value,
                radius = indicatorRadius,
                center = currentSelection,
                style = Stroke(indicatorWidth),
            )
        }
    }
}



@Preview
@Composable
fun PreviewColorSelector() {
    LibraSheetTheme {
        Surface {
            ColorSelector(
                currentColor = PolarColor(
                    r = 1f,
                    phi = 0f,
                    color = Color(255, 0, 0)
                ),
                brightness = 0.8f,
                modifier = Modifier.size(360.dp)
            )
        }
    }
}