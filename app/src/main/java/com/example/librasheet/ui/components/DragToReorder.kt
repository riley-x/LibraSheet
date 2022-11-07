package com.example.librasheet.ui.components

import android.util.Log
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

class DragInfo {
    var index by mutableStateOf(-1)
    var height by mutableStateOf(0)

    var offset by mutableStateOf(0f)
    var currentY by mutableStateOf(0f)

    fun reset() {
        index = -1
        offset = 0f
        height = 0
        currentY = 0f
    }
}
//val LocalDragInfo = compositionLocalOf { DragInfo() }
//val dragInfo = LocalDragInfo.current

@Composable
fun DragToReorder(
    dragInfo: DragInfo,
    index: Int,
    content: @Composable () -> Unit = { },
) {
    val haptic = LocalHapticFeedback.current

    val zIndex = if (index == dragInfo.index) 10f else 0f
    var height by remember { mutableStateOf(0) }
    var originalY by remember { mutableStateOf(0f) }

    fun getOffset() =
        if (index == dragInfo.index) dragInfo.offset.roundToInt()
        else if (index > dragInfo.index) {
            val targetY = originalY - dragInfo.height
            val thresholdY = targetY + height - minOf(height, dragInfo.height) / 2f
            if (dragInfo.currentY > thresholdY) -dragInfo.height
            else 0
        }
        else if (index < dragInfo.index) {
            val thresholdY = originalY + minOf(height, dragInfo.height) / 2f
            if (dragInfo.currentY < thresholdY) dragInfo.height
            else 0
        }
        else 0


    val offset =
        if (index != dragInfo.index) {
            val targetOffset by remember { derivedStateOf { getOffset() } }
            animateIntAsState(targetValue = targetOffset).value
        }
        else getOffset()


    if (index == 2) Log.d("Libra", "$offset")

    Box(
        modifier = Modifier
            .onGloballyPositioned {
                height = it.size.height
                originalY = it.localToRoot(Offset.Zero).y
            }
            .offset { IntOffset(0, offset) }
            .background(MaterialTheme.colors.surface)
            .zIndex(zIndex)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        Log.d("Libra", "Drag Start: $index")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dragInfo.index = index
                        dragInfo.height = height
                        dragInfo.offset = 0f
                        dragInfo.currentY = originalY
                    },
                    onDragEnd = {
                        Log.d("Libra", "Drag End: $index")
                        dragInfo.reset()
                    },
                    onDragCancel = {
                        Log.d("Libra", "Drag Cancel: $index")
                        dragInfo.reset()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragInfo.offset += dragAmount.y
                        dragInfo.currentY += dragAmount.y
                    }
                )
            }
    ) {
        content()
    }
}