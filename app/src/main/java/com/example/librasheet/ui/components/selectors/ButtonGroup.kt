package com.example.librasheet.ui.components.selectors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.CategoryTimeRange
import com.example.librasheet.viewModel.categoryTimeRanges
import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.dataClasses.NameOrIcon

@Composable
fun <T: HasDisplayName> ButtonGroup(
    options: ImmutableList<T>,
    currentSelection: State<T>, // must pass State here for derivedStateOf below
    modifier: Modifier = Modifier,
    onSelection: (T) -> Unit = { },
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        for (option in options.items) {
            // enabled would be recalculated for each button, but only 2 of them need to recompose
            val enabled by remember { derivedStateOf { option == currentSelection.value } }
            CustomTextButton(
                text = option.displayName,
                enabled = enabled,
                onSelection = { onSelection(option) },
            )
        }
    }
}

@Composable
fun <T: NameOrIcon> TextOrIconButtons(
    options: ImmutableList<T>,
    currentSelection: State<T>, // must pass State here for derivedStateOf below
    modifier: Modifier = Modifier,
    onSelection: (T) -> Unit = { },
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        for (option in options.items) {
            // enabled would be recalculated for each button, but only 2 of them need to recompose
            val enabled by remember { derivedStateOf { option == currentSelection.value } }
            if (option.icon != null) {
                Icon(
                    imageVector = option.icon!!,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.disabled),
                    modifier = Modifier.clickable { onSelection(option) }
                )
            } else {
                CustomTextButton(
                    text = option.displayName,
                    enabled = enabled,
                    onSelection = { onSelection(option) },
                )
            }
        }
    }
}


/**
 * The normal TextButton has too much padding.
 * Also, scoping this function helps prevent recomposition (otherwise ternaries
 * like
 *      if (enabled) MaterialTheme.colors.primary else MaterialTheme.colors.background
 * would trigger recomposition
 */
@Composable
fun CustomTextButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    colorBackground: Color = MaterialTheme.colors.background,
    colorText: Color =  MaterialTheme.colors.primary,
    colorBackgroundDisabled: Color = MaterialTheme.colors.background,
    colorTextDisabled: Color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.disabled),
    onSelection: (String) -> Unit = { },
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (enabled) colorBackground else colorBackgroundDisabled,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = MaterialTheme.colors.onBackground),
            ) {
                onSelection(text)
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
        ) {
            Text(
                text = text,
                color = if (enabled) colorText else colorTextDisabled,
                style = MaterialTheme.typography.button,
            )
        }
    }
}


@Preview
@Composable
private fun Preview() {
    val cur = remember { mutableStateOf(CategoryTimeRange.ONE_MONTH) }
    LibraSheetTheme {
        Surface {
            ButtonGroup(
                options = categoryTimeRanges,
                currentSelection = cur,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewIcon() {
    val cur = remember { mutableStateOf(CategoryTimeRange.ONE_MONTH) }
    LibraSheetTheme {
        Surface {
            TextOrIconButtons(
                options = categoryTimeRanges,
                currentSelection = cur,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

