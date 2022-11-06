package com.example.librasheet.ui.settings

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils
import com.example.librasheet.ui.components.ImmutableList


val colors = ImmutableList(listOf(
    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
))


@Immutable
data class PolarColor(
    val r: Float,
    val phi: Float,
    val color: Color,
) {
    companion object Factory {
        operator fun invoke(
            color: Color
        ): PolarColor {
            val brightness = brightness(color)
            val polar = colorToPolar(color, brightness)
            return PolarColor(
                polar.first,
                polar.second,
                color
            )
        }

        operator fun invoke(
            r: Float,
            phi: Float,
            brightness: Float,
        ) = PolarColor(r, phi, colorFromPolar(r, phi, brightness))
    }
}

/** Solves for the original color from blendedColor = blend(origColor, filterColor, ratio) **/
fun unblend(blendedColor: Color, filterColor: Color, ratio: Float): Color {
    if (ratio <= 0) return blendedColor
    if (ratio >= 1) return filterColor

    fun eval(fn: Color.() -> Float) = MathUtils.clamp(
        (blendedColor.fn() - filterColor.fn() * ratio) / (1 - ratio),
        0f, 1f
    )
    return Color(eval(Color::red), eval(Color::green), eval(Color::blue))
}

// THIS DOESN'T WORK WHEN ORIGINAL BRIGHTNESS IS 0!
//fun redarken(blendedColor: Color, originalBrightness: Float, newBrightness: Float): Color {
//    fun eval(fn: Color.() -> Float) = blendedColor.fn() * newBrightness / originalBrightness
//    return Color(eval(Color::red), eval(Color::green), eval(Color::blue))
//}


fun colorFromPolar(rPhi: Pair<Float, Float>, brightness: Float) = colorFromPolar(rPhi.first, rPhi.second, brightness)
fun colorFromPolar(r: Float, phi: Float, brightness: Float): Color {
    val stopIndex = phi.toInt() / 60

    val hue = ColorUtils.blendARGB(
        colors.items[stopIndex].toArgb(),
        colors.items[stopIndex + 1].toArgb(),
        phi / 60 - stopIndex,
    )
    val sat = ColorUtils.blendARGB(Color.White.toArgb(), hue, r)
    return Color(ColorUtils.blendARGB(Color.Black.toArgb(), sat, brightness))
}

fun colorToPolar(color: Color, brightness: Float): Pair<Float, Float> {
    if (brightness <= 0f) return Pair(0f, 0f) // Can't get polar coordinates if fully black

    val noBlack = unblend(color, Color.Black, 1 - brightness)

    /** At the outer edge, one color is exactly 0. So the fraction of white is simply the
     * smallest value **/
    val radius = 1 - minOf(noBlack.red, noBlack.green, noBlack.blue)
    val noWhite = unblend(noBlack, Color.White, 1 - radius)

    /** At the outer edge, the primary colors have one field = 1, while the secondary colors
     * have two fields = 1. So the fraction of the secondary is simply the smaller value **/
    val r = noWhite.red
    val g = noWhite.green
    val b = noWhite.blue

    val phi = if (r >= g && g >= b) 60 * g // red-yellow
    else if (g >= r && r >= b) 60 + 60 * (1 - r) // yellow-green
    else if (g >= b && b >= r) 120 + 60 * b // green-cyan
    else if (b >= g && g >= r) 180 + 60 * (1 - g) // cyan- blue
    else if (b >= r && r >= g) 240 + 60 * r // blue-magenta
    else 300 + 60 * (1 - b) // magenta-red

    return Pair(radius, phi)
}

/** Max brightness == one color is 255 **/
fun brightness(color: Color) = maxOf(color.red, color.green, color.blue)

