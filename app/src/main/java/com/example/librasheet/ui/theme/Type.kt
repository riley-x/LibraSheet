package com.example.librasheet.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.librasheet.R

val EczarFontFamily = FontFamily(
    Font(R.font.eczar_regular),
    Font(R.font.eczar_semibold, FontWeight.SemiBold)
)
val RobotoCondensed = FontFamily(
    Font(R.font.robotocondensed_regular),
    Font(R.font.robotocondensed_light, FontWeight.Light),
    Font(R.font.robotocondensed_bold, FontWeight.Bold)
)
val Roboto = FontFamily(
    Font(R.font.roboto_regular),
    Font(R.font.roboto_bold, FontWeight.Bold)
)
val UmTypewriter = FontFamily(
    Font(R.font.um_typewriter),
)

val Typography = Typography(
    defaultFontFamily = RobotoCondensed,
    // Header bars
    h1 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        fontFamily = EczarFontFamily,
        letterSpacing = 1.5.sp,
        lineHeight = 26.sp,
    ),
    // Pie chart, list titles, card titles
    h2 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        fontFamily = EczarFontFamily,
    ),
    h3 = TextStyle(
        fontWeight = FontWeight.W100,
        fontSize = 22.sp,
        fontFamily = EczarFontFamily,
    ),
    h4 = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 22.sp,
        letterSpacing = 0.5.sp,
        fontFamily = Roboto,
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = 18.sp,
        letterSpacing = 1.sp,
        fontFamily = Roboto,
    ),
    h6 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        fontFamily = Roboto,
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 2.5.sp
    ),
    subtitle2 = TextStyle( // Monospaced, i.e. account numbers
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        fontFamily = UmTypewriter,
        letterSpacing = 4.sp
    ),
    body1 = TextStyle( // Default text
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 1.5.sp
    ),
    body2 = TextStyle( // Sub-text
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.5.sp
    ),
    button = TextStyle( // Used by TextButtons
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.sp,
    ),
    caption = TextStyle( // Used by OutlinedTextField for the label. Also transaction filters
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    overline = TextStyle( // Monospaced for graph/etc.
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        fontFamily = UmTypewriter,
    ),
)