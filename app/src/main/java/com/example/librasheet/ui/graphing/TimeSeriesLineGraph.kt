package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewBalanceHistoryState


fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)
fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)

@Composable
fun lineGraph(
    color: Color = MaterialTheme.colors.onSurface,
    size: Float = with(LocalDensity.current) { 2.dp.toPx() },
): TimeSeriesGrapher<Float> {
    return fun(
        drawScope: DrawScope,
        values: List<Float>,
        deltaX: Float,
        deltaY: Float,
        startY: Float,
        minX: Float,
        minY: Float,
    ) {
        fun loc(i: Int) = Offset(
            x = deltaX * (i - minX),
            y = startY + deltaY * (values[i] - minY)
        )

        val path = Path().apply {
            moveTo(loc(0))
            for (i in 1 until values.size) {
                 lineTo(loc(i))
            }
        }

        drawScope.drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = size,
            )
        )
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
