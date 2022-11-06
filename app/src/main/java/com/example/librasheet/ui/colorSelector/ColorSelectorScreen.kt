package com.example.librasheet.ui.colorSelector

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.components.HeaderBar
import com.example.librasheet.ui.theme.LibraSheetTheme
import kotlin.math.*

@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun ColorSelectorScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    title: String = "Pick Color",
    initialColor: Color = Color.White,
    onSave: (Color) -> Unit = { },
    onCancel: () -> Unit = { },
) {
    /** Keyboard and focus **/
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    fun clearFocus() {
        keyboardController?.hide()
        focusManager.clearFocus(true)
    }

    /** Color wheel selection state **/
    var currentColor by remember { mutableStateOf(PolarColor(initialColor)) }
    var brightness by remember { mutableStateOf(brightness(initialColor)) }

    /** String selection state **/
    var red by remember { mutableStateOf((initialColor.red * 255).roundToInt().toString())}
    var green by remember { mutableStateOf((initialColor.green * 255).roundToInt().toString())}
    var blue by remember { mutableStateOf((initialColor.blue * 255).roundToInt().toString())}

    /** Synchronizing string and color wheel states **/
    fun updateSelection() {
        val r = red.toIntOrNull()
        val g = green.toIntOrNull()
        val b = blue.toIntOrNull()
        if (r == null || g == null || b == null) return
        if (r !in 0..255 || g !in 0..255 || b !in 0..255) return
        currentColor = PolarColor(Color(red.toInt(), green.toInt(), blue.toInt()))
        brightness = brightness(currentColor.color)
    }
    fun updateStrings() {
        red = (currentColor.color.red * 255).roundToInt().toString()
        green = (currentColor.color.green * 255).roundToInt().toString()
        blue = (currentColor.color.blue * 255).roundToInt().toString()
    }

    /** Callbacks
     * These need to be declared here and not as lambdas or else, for example, changing the wheel
     * selector will cause the brightness bar to recompomse **/
    fun onSelection(r: Float, phi: Float) {
        currentColor = PolarColor(r = r, phi = phi, brightness = brightness)
        updateStrings()
    }
    fun onBrightness(it: Float) {
        brightness = it
        currentColor = PolarColor(r = currentColor.r, phi = currentColor.phi, brightness = brightness)
        updateStrings()
        clearFocus()
    }
    fun onRed(it: String) {
        if (it.length <= 3) {
            red = it
            updateSelection()
        }
    }
    fun onGreen(it: String) {
        if (it.length <= 3) {
            green = it
            updateSelection()
        }
    }
    fun onBlue(it: String) {
        if (it.length <= 3) {
            blue = it
            updateSelection()
        }
    }
    fun onClickSave() = onSave(currentColor.color)

    /** Composable **/
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            /** These paddings need to be placed after the scroll modifier or else they will cause
             * a flicker. The only problem with this is that the bottom ripple doesn't appear anymore.
             */
            .windowInsetsPadding(WindowInsets.ime)
            .padding(bottom = if (WindowInsets.isImeVisible) 0.dp else bottomPadding)
    ) {
        HeaderBar(title = title, backArrow = true, onBack = onCancel)

        ColorSelector(
            currentColor = currentColor,
            brightness = brightness,
            onPress = ::clearFocus,
            onChange = ::onSelection,
            modifier = Modifier
        )

        BrightnessSelector(
            brightness = brightness,
            onChange = ::onBrightness,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 20.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            val keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                autoCorrect = false,
                imeAction = ImeAction.Next,
            )
            OutlinedTextField(
                value = red,
                label = { Text("Red") },
                onValueChange = ::onRed,
                colors = textFieldColors(MaterialTheme.colors.error),
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(10f)
            )
            OutlinedTextField(
                value = green,
                label = { Text("Green") },
                onValueChange = ::onGreen,
                colors = textFieldColors(MaterialTheme.colors.primary),
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(10f)
            )
            OutlinedTextField(
                value = blue,
                label = { Text("Blue") },
                onValueChange = ::onBlue,
                colors = textFieldColors(Color(94, 131, 241, 255)),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    autoCorrect = false,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { clearFocus() }
                ),
                modifier = Modifier.weight(10f)
            )
        }

        Spacer(Modifier.height(30.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Old")
                Canvas(modifier = Modifier.size(60.dp)) {
                    drawRect(initialColor)
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("New")
                Canvas(modifier = Modifier.size(60.dp)) {
                    drawRect(currentColor.color)
                }

            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error,
                ),
                modifier = Modifier.width(100.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = ::onClickSave,
                modifier = Modifier.width(100.dp)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun textFieldColors(c: Color) = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = c.copy(alpha = ContentAlpha.high),
    unfocusedBorderColor = c.copy(alpha = ContentAlpha.disabled),
    focusedLabelColor = c.copy(alpha = ContentAlpha.high),
    unfocusedLabelColor = c.copy(alpha = ContentAlpha.medium),
)


@Preview(
    widthDp = 360,
    heightDp = 740,
)
@Composable
fun PreviewColorSelectorScreen() {
    LibraSheetTheme {
        Surface {
            ColorSelectorScreen(
            )
        }
    }
}