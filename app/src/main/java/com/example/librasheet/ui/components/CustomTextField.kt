package com.example.librasheet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme


/**
 * This works pretty well, except selecting the text box puts the cursor at the start, wherever you
 * click.
 */
@Composable
fun CustomTextField(
    text: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    number: Boolean = true,
    error: Boolean = false,
    onValueChange: (String) -> Unit = { },
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    val lines = if (number) 1 else 3
    val textIsEmpty by remember(text) { derivedStateOf { text.isEmpty() } }

    BasicTextField(
        value = text,
        onValueChange = onValueChange,
        singleLine = lines == 1,
        maxLines = lines,
        textStyle = MaterialTheme.typography.body1.copy(
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.high)
        ),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus(true) }
        ),
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
    ) {
        Box(
            contentAlignment = if (lines == 1) Alignment.CenterStart else Alignment.TopStart,
            modifier = Modifier.fillMaxSize()
        ) {
            if (focused) {
                Box(
                    Modifier.padding(6.dp)
                ) {
                    it()
                }
            } else { // Need this since BasicTextField doesn't have TextOverflow
                Text(
                    text = if (textIsEmpty) placeholder else text,
                    maxLines = lines,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colors.onSurface.copy(
                        alpha = if (textIsEmpty) ContentAlpha.disabled else ContentAlpha.high
                    ),
                    modifier = Modifier.padding(6.dp)
                )
            }

            val color =
                if (focused) MaterialTheme.colors.primary
                else if (error) MaterialTheme.colors.error
                else Color.Unspecified
            val radius: Float
            val width: Float
            with(LocalDensity.current) {
                radius = 10.dp.toPx()
                width = 1.dp.toPx()
            }
            Canvas(modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)) {
                drawRoundRect(
                    color = color,
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = width),
                )
            }
        }
    }
}



@Composable
fun OutlinedTextFieldNoPadding(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
    contentPadding: PaddingValues = PaddingValues(), // !ADDED
) {
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled).value
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    @OptIn(ExperimentalMaterialApi::class)
    BasicTextField(
        value = value,
        modifier = if (label != null) {
            modifier
                // Merge semantics at the beginning of the modifier chain to ensure padding is
                // considered part of the text field.
                .semantics(mergeDescendants = true) {}
//                .padding(top = OutlinedTextFieldTopPadding)
        } else {
            modifier
        }
            .background(colors.backgroundColor(enabled).value, shape),
//            .defaultMinSize(
//                minWidth = TextFieldDefaults.MinWidth,
//                minHeight = TextFieldDefaults.MinHeight
//            ),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colors.cursorColor(isError).value),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = contentPadding, // !ADDED
                border = {
                    TextFieldDefaults.BorderBox(
                        enabled,
                        isError,
                        interactionSource,
                        colors,
                        shape
                    )
                }
            )
        }
    )
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            CustomTextField(text = "Hello!", modifier = Modifier.height(50.dp))
        }
    }
}


@Preview
@Composable
private fun PreviewOutline() {
    LibraSheetTheme {
        Surface {
            OutlinedTextFieldNoPadding(value = "Hello!", onValueChange = { }, contentPadding = PaddingValues(6.dp))
        }
    }
}