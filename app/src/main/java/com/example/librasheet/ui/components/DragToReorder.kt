package com.example.librasheet.ui.components

import android.util.Log
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
    var parentId by mutableStateOf(-1)
    var height by mutableStateOf(0)

    var offset by mutableStateOf(0f)
    var currentY by mutableStateOf(0f)

    fun reset() {
        index = -1
        parentId = -1
        offset = 0f
        height = 0
        currentY = 0f
    }
}
val LocalDragInfo = compositionLocalOf { DragInfo() }

@Composable
fun DragToReorder(
    index: Int,
    parentId: Int,
    content: @Composable () -> Unit = { },
) {
    Log.d("Libra", "index=$index parentId=$parentId")

    val haptic = LocalHapticFeedback.current
    val dragInfo = LocalDragInfo.current

    val zIndex = if (parentId == dragInfo.parentId && index == dragInfo.index) 10f else 0f
    var height by remember { mutableStateOf(0) }
    var originalY by remember { mutableStateOf(0f) }
    val offset by remember(dragInfo) { derivedStateOf {
        if (parentId != dragInfo.parentId) 0
        else if (index == dragInfo.index) dragInfo.offset.roundToInt()
        else if (index > dragInfo.index) {
            val targetY = originalY - dragInfo.height
            val thresholdY = targetY + height - minOf(height, dragInfo.height) / 2f
            if (dragInfo.currentY > thresholdY) -dragInfo.height
            else 0
        }
        else if (index < dragInfo.index)  {
            val thresholdY = originalY + minOf(height, dragInfo.height) / 2f
            if (dragInfo.currentY < thresholdY) dragInfo.height
            else 0
        }
        else 0
    } }
    if (index == 2 && parentId == 0)
        Log.d("Libra", "height=$height originalY=$originalY")

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
                        Log.d("Libra", "Drag Start: $index $parentId")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dragInfo.index = index
                        dragInfo.parentId = parentId
                        dragInfo.height = height
                        dragInfo.offset = 0f
                        dragInfo.currentY = originalY
                    },
                    onDragEnd = {
                        Log.d("Libra", "Drag End: $index $parentId")
                        dragInfo.reset()
                    },
                    onDragCancel = {
                        Log.d("Libra", "Drag Cancel: $index $parentId")
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