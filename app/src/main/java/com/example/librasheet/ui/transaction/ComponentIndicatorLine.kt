package com.example.librasheet.ui.transaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

/**
 * This draws a right-angled line that indicates a nested heirarchy or child
 * component(s).
 */

@Composable
fun ComponentIndicatorLine(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
    strokeWidth: Float = 5f,
    last: Boolean = false,
) {
    Canvas(modifier = modifier) {
        val halfSize = size / 2.0f
        drawLine(
            color = color,
            start = Offset(strokeWidth / 2f, 0f),
            end = Offset(strokeWidth / 2f, if (last) halfSize.height + strokeWidth / 2f else size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(strokeWidth, halfSize.height),
            end = Offset(size.width, halfSize.height),
            strokeWidth = strokeWidth
        )
    }
}

@Preview
@Composable
fun PreviewComponentIndicatorLine() {
    LibraSheetTheme {
        Surface {
            ComponentIndicatorLine(Modifier.size(52.dp))
        }
    }
}

@Preview
@Composable
fun PreviewComponentIndicatorLine2() {
    LibraSheetTheme {
        Surface {
            ComponentIndicatorLine(Modifier.size(52.dp), last = true)
        }
    }
}