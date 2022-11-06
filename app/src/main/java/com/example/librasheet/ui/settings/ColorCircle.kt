package com.example.librasheet.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme


@Composable
fun ColorCircle(
    brightness: Float,
    modifier: Modifier = Modifier,
    overlay: DrawScope.() -> Unit = { },
) {
    Canvas(
        modifier
            .aspectRatio(1f)
            .fillMaxSize()) {
        drawCircle(brush = Brush.sweepGradient(colors.items))

        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White, Color.Transparent)
            )
        )

        drawCircle(color = Color.Black.copy(alpha = 1 - brightness))

        overlay()
    }
}



@Preview
@Composable
fun PreviewColorCircle() {
    LibraSheetTheme {
        Surface(Modifier.size(300.dp)) {
            ColorCircle(brightness = 0.7f)
        }
    }
}
