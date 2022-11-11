package com.example.librasheet.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIos
import androidx.compose.material.icons.sharp.ArrowForwardIos
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.previewGraphLabels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DialSelector(
    selected: State<Int>,
    labels: ImmutableList<String>,
    modifier: Modifier = Modifier,
    onSelection: (Int, wasFromDialRight: Boolean) -> Unit = { _, _ -> },
) {
    val scope = rememberCoroutineScope()
    var swipeWidth by remember { mutableStateOf(500f) }
    val swipeableState = rememberSwipeableState(1)
    val anchors = remember(swipeWidth) {
        (0..labels.items.size + 1).associateBy(keySelector = { it * swipeWidth }, valueTransform = { it })
    }
    Log.d("Libra", "${swipeableState.currentValue}")

    LaunchedEffect(swipeableState.currentValue) {
        Log.d("Libra", "enter ${swipeableState.currentValue}")
        if (swipeableState.currentValue == 0) swipeableState.snapTo(labels.items.size)
        else if (swipeableState.currentValue == labels.items.size + 1) swipeableState.snapTo(1)
        Log.d("Libra", "exit ${swipeableState.currentValue}")
    }

    @Stable
    fun get(stateValue: Int): String {
        val index = (stateValue - 1 + labels.items.size) % labels.items.size
        return labels.items[index]
    }

    fun back() {
        scope.launch {
            if (swipeableState.currentValue == 0)
                swipeableState.snapTo(labels.items.size + 1)
            swipeableState.animateTo(swipeableState.currentValue - 1)
        }
    }
    fun next() {
        scope.launch {
            if (swipeableState.currentValue == labels.items.size + 1)
                swipeableState.snapTo(0)
            swipeableState.animateTo(swipeableState.currentValue + 1)
        }
    }


    Surface(modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier

            ) {
                CompositionLocalProvider(
                    LocalMinimumTouchTargetEnforcement provides false,
                ) {
                    IconButton(
                        onClick = ::back,
                        modifier = Modifier
                            .background(MaterialTheme.colors.background)
                            .zIndex(1f)
                    ) {
                        Icon(imageVector = Icons.Sharp.ArrowBackIos, contentDescription = null)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(10f)
                            .onGloballyPositioned { swipeWidth = it.size.width.toFloat() }
                            .swipeable(
                                state = swipeableState,
                                anchors = anchors,
                                orientation = Orientation.Horizontal,
                                reverseDirection = true, // "natural" scrolling
                            )
                    ) {
                        val offset = swipeableState.currentValue * swipeWidth - swipeableState.offset.value
                        Text(
                            text = get(swipeableState.currentValue - 1),
                            style = MaterialTheme.typography.h2,
                            modifier = Modifier.offset { IntOffset((offset - swipeWidth).roundToInt(), 0) }
                        )
                        Text(
                            text = get(swipeableState.currentValue),
                            style = MaterialTheme.typography.h2,
                            modifier = Modifier.offset { IntOffset(offset.roundToInt(), 0) }
                        )
                        Text(
                            text = get(swipeableState.currentValue + 1),
                            style = MaterialTheme.typography.h2,
                            modifier = Modifier.offset { IntOffset((offset + swipeWidth).roundToInt(), 0) }
                        )
                    }

                    IconButton(
                        onClick = ::next,
                        modifier = Modifier.background(MaterialTheme.colors.background)
                    ) {
                        Icon(imageVector = Icons.Sharp.ArrowForwardIos, contentDescription = null)
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                labels.items.indices.forEach {
                    Canvas(modifier = Modifier.size(7.dp)) {
                        val r = size.minDimension / 2.0f
                        val border = 1.dp.toPx()
                        drawCircle(
                            color = color,
                            radius = if (it == selected.value) r else r - border / 2f,
                            style = if (it == selected.value) Fill else Stroke(width = border)
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    val selected = remember { mutableStateOf(0) }
    LibraSheetTheme {
        Surface {
            DialSelector(selected = selected, labels = previewGraphLabels)
        }
    }
}