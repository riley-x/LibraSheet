package com.example.librasheet.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.BrightnessHigh
import androidx.compose.material.icons.sharp.BrightnessLow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme

@Composable
fun BrightnessSelector(
    brightness: Float,
    modifier: Modifier = Modifier,
    onChange: (Float) -> Unit = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Sharp.BrightnessLow,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )

        Slider(
            value = brightness,
            onValueChange = onChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colors.onSurface,
                activeTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            ),
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .weight(10f) // So that the second icon isn't hidden
        )

        Icon(
            imageVector = Icons.Sharp.BrightnessHigh,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            BrightnessSelector(0.5f)
        }
    }
}