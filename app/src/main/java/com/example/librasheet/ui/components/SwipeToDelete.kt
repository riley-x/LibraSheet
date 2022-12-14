package com.example.librasheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.librasheet.ui.settings.CategoryRulesScreen
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewRules
import kotlin.math.roundToInt


@Composable
fun RemoveBanner(
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
            .height(libraRowHeight)
            .fillMaxWidth()
            .background(MaterialTheme.colors.error)
    ) {
        Text(
            text = "Remove",
            modifier = Modifier.padding(end = libraRowHorizontalPadding),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.h6,
        )
    }
}



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDelete(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = { },
    content: @Composable BoxScope.() -> Unit = { },
) {
    val swipeableState = rememberSwipeableState(0)
    var sizePx by remember { mutableStateOf(1000) }
    val anchors = mapOf(0f to 0, sizePx.toFloat() to 1)

    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue == 1) {
            onDelete()
            swipeableState.snapTo(0)
        }
    }

    Box {
        RemoveBanner()
        Box(
            modifier = modifier
                .onGloballyPositioned { sizePx = it.size.width }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.5f) },
                    orientation = Orientation.Horizontal,
                    reverseDirection = true,
//                    resistance = null, // disable. the problem with this is that it still captures the dragging, which makes lazy lists unresponsive.
                    velocityThreshold = 5000.dp, // default is 125
                )
                .offset { IntOffset(-swipeableState.offset.value.roundToInt(), 0) }
        ) {
            content()
        }
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            RemoveBanner()
        }
    }
}