package com.example.librasheet.ui.components.textFields

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.librasheet.ui.theme.LibraSheetTheme


@OptIn(ExperimentalTextApi::class)
fun Modifier.textFieldBorder(label: String = "") = composed {
    val textMeasurer = rememberTextMeasurer()
    val layoutResult = textMeasurer.measure(
        text = AnnotatedString(label),
        style = MaterialTheme.typography.subtitle1.copy(
//            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.W500,
        ),
    )
    val background = MaterialTheme.colors.background
    val textColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
    val padding = with (LocalDensity.current) { 16.dp.toPx() }
    val backgroundPadding = with (LocalDensity.current) { 4.dp.toPx() }
    val topLeft = Offset(padding, -layoutResult.size.height / 2f)
    val topPadding = with (LocalDensity.current) { (layoutResult.size.height / 2f).toDp() }
    var out = this
    if (label.isNotEmpty()) {
        out = out
            .padding(top = topPadding)
            .drawWithCache {
            onDrawWithContent {
                drawContent()
                drawRect(
                    color = background,
                    topLeft = topLeft.copy(x = topLeft.x - backgroundPadding),
                    size = Size(
                        layoutResult.size.width.toFloat() + 2 * backgroundPadding,
                        layoutResult.size.height.toFloat()
                    )
                )
                drawText(
                    layoutResult,
                    topLeft = topLeft,
                    color = textColor,
                )
            }
        }
    }
    return@composed out
        .border(
            width = 1.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
            shape = MaterialTheme.shapes.small,
        )
        .padding(horizontal = 16.dp, vertical = 4.dp)
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            Text(
                "Hello! This is a fake outlined text field!",
                Modifier
                    .textFieldBorder("Label!")
                    .padding(vertical = 12.dp)
            )
        }
    }
}