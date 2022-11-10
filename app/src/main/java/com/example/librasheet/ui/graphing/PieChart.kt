package com.example.librasheet.ui.graphing

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.components.formatPercent
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.Account
import com.example.librasheet.viewModel.preview.previewAccounts
import java.lang.Math.toDegrees

private const val DividerLengthInDegrees = 1.8f

interface PieChartValue {
    val name: String
    val value: Float
    val color: Color
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T: PieChartValue> PieChart(
    values: List<T>,
    modifier: Modifier = Modifier,
    stroke: Dp = 30.dp,
) {
    val total = values.sumOf { it.value.toDouble() }.toFloat()
    val angles = FloatArray(values.size + 1) // first entry is 0, last entry is 360
    for (index in values.indices) {
        angles[index + 1] = angles[index] + 360f * values[index].value / total
    }

    var boxSize by remember { mutableStateOf(IntSize(0, 0)) }
    var focusIndex by remember { mutableStateOf(-1) }
    val disallowIntercept = RequestDisallowInterceptTouchEvent()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .onGloballyPositioned { boxSize = it.size }
            .pointerInteropFilter(
                requestDisallowInterceptTouchEvent = disallowIntercept
            ) { motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_MOVE ||
                    motionEvent.action == MotionEvent.ACTION_DOWN) {
                    disallowIntercept(true)
                    val x = motionEvent.x - boxSize.width / 2
                    val y = motionEvent.y - boxSize.height / 2

                    // Here we map y -> (Right = +x) and x -> (Up = -y)
                    // since we start at the top and move clockwise
                    var phi = toDegrees(kotlin.math.atan2(x, -y).toDouble())
                    if (phi < 0) phi += 360

                    val index = angles.indexOfFirst { it > phi }
                    focusIndex = index - 1
                } else {
                    disallowIntercept(false)
                    focusIndex = -1
                }
                true
            }
    ) {
        val strokeNormalPx = with(LocalDensity.current) { Stroke(stroke.toPx()) }
        val strokeFocusPx = with(LocalDensity.current) { Stroke((10.dp + stroke).toPx()) }
        Canvas(Modifier.fillMaxSize()) {
            val innerRadius = (size.minDimension - strokeFocusPx.width) / 2
            val halfSize = size / 2.0f
            val topLeft = Offset(
                halfSize.width - innerRadius,
                halfSize.height - innerRadius
            )
            val size = Size(innerRadius * 2, innerRadius * 2)
            var startAngle = -90f
            val totalAngle = 360f
            values.forEachIndexed { index, account ->
                val sweep = totalAngle * account.value / total
                drawArc(
                    color = account.color,
                    startAngle = startAngle + DividerLengthInDegrees / 2,
                    sweepAngle = sweep - DividerLengthInDegrees,
                    topLeft = topLeft,
                    size = size,
                    useCenter = false,
                    style = if (focusIndex == index) strokeFocusPx else strokeNormalPx
                )
                startAngle += sweep
            }
        }
        val centerText = if (focusIndex < 0 || focusIndex >= values.size) {
            "Total\n" + formatDollar(total)
        } else {
            values[focusIndex].name + "\n" + formatPercent(values[focusIndex].value / total)
        }
        Text(centerText,
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center,
        )
    }
}



@Composable
fun <T: PieChartValue> PieChartFiltered(
    values: SnapshotStateList<T>,
    modifier: Modifier = Modifier,
) {
    val filteredValues = values.filter { it.value > 0 }
    if (filteredValues.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            Text("No Data",
                style = MaterialTheme.typography.h2,
                color = MaterialTheme.colors.error.copy(alpha = ContentAlpha.medium),
            )
        }
    } else {
        PieChart(filteredValues, modifier)
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            PieChart(
                values = previewAccounts,
                modifier = Modifier.size(300.dp),
            )
        }
    }
}