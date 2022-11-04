package com.example.zygos.ui.graphing

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.HasName
import com.example.librasheet.viewModel.dataClasses.HasValue
import com.example.librasheet.viewModel.preview.previewBalanceHistoryState


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
        for (i in 1 until values.size) {
            drawScope.drawLine(
                start = Offset(
                    x = deltaX * (i - 1 - minX),
                    y = startY + deltaY * (values[i - 1] - minY)
                ),
                end = Offset(
                    x = deltaX * (i - minX),
                    y = startY + deltaY * (values[i] - minY)
                ),
                color = color,
                strokeWidth = size,
            )
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
