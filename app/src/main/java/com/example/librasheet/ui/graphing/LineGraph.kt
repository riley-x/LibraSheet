package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewBalanceHistory
import com.example.librasheet.viewModel.preview.previewBalanceHistoryAxes


@Composable
fun lineGraph(
    values: List<Float>,
    color: Color = MaterialTheme.colors.onSurface,
    size: Float = with(LocalDensity.current) { 2.dp.toPx() },
): Grapher {
    return fun DrawScope.(
        userToPxX: (Float) -> Float,
        userToPxY: (Float) -> Float,
    ) {
        fun loc(i: Int) = Offset(
            x = userToPxX(i.toFloat()),
            y = userToPxY(values[i])
        )

        val path = Path().apply {
            moveTo(loc(0))
            for (i in 1 until values.size) {
                 lineTo(loc(i))
            }
        }

        drawPath(
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
            Graph(
                axesState = previewBalanceHistoryAxes,
                content = lineGraph(previewBalanceHistory),
                modifier = Modifier.size(360.dp, 360.dp)
            )
        }
    }
}
