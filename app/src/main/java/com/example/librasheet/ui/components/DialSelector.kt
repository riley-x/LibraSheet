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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    labels: ImmutableList<String>,
    modifier: Modifier = Modifier,
    swipeableState: SwipeableState<Int> = rememberSwipeableState(0),
    onSelection: (Int, looped: Boolean) -> Unit = { _, _ -> },
) {
    @Stable
    fun index(stateValue: Int) = (stateValue + labels.items.size) % labels.items.size
    @Stable
    fun get(stateValue: Int) = labels.items[index(stateValue)]

    val scope = rememberCoroutineScope()
    var swipeWidth by rememberSaveable { mutableStateOf(1000f) }

    /** I think it's necessary that the initial state has anchor 0f, or else an animation will trigger
     * when the composable is first loaded. Also important to save the swipeWidth above, and set it
     * to something big so the side text doesn't display. **/
    val anchors = remember(swipeWidth) {
        (-1..labels.items.size).associateBy(keySelector = { it * swipeWidth }, valueTransform = { it })
    }

    Log.d("Libra", "composable ${swipeableState.currentValue}")
    LaunchedEffect(swipeableState.currentValue) {
        Log.d("Libra", "enter ${swipeableState.currentValue}")
        val loop: Boolean
        val index: Int
        if (swipeableState.currentValue == -1) {
            loop = true
            index = labels.items.lastIndex
            swipeableState.snapTo(index)
        }
        else if (swipeableState.currentValue == labels.items.size) {
            loop = true
            index = 0
            swipeableState.snapTo(index)
        } else {
            loop = false
            index = swipeableState.currentValue
        }
        Log.d("Libra", "exit ${swipeableState.currentValue} $index")
        onSelection(index, loop)
    }


    fun back() {
        scope.launch {
            if (swipeableState.currentValue == -1)
                swipeableState.snapTo(labels.items.size)
            swipeableState.animateTo(swipeableState.currentValue - 1)
        }
    }
    fun next() {
        scope.launch {
            if (swipeableState.currentValue == labels.items.size)
                swipeableState.snapTo(-1)
            swipeableState.animateTo(swipeableState.currentValue + 1)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalMinimumTouchTargetEnforcement provides false,
            ) {
                IconButton(onClick = ::back) {
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
                        .clipToBounds()
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

                IconButton(onClick = ::next) {
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
                        radius = if (it == index(swipeableState.currentValue)) r else r - border / 2f,
                        style = if (it == index(swipeableState.currentValue)) Fill else Stroke(width = border)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
private fun Preview() {
    val selected = remember { mutableStateOf(0) }
    LibraSheetTheme {
        Surface {
            DialSelector(labels = previewGraphLabels)
        }
    }
}