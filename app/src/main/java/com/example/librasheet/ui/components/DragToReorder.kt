package com.example.librasheet.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

class DragScope {
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

fun Modifier.dragToReorder(
    dragScope: DragScope,
    index: Int,
) = composed {
    val haptic = LocalHapticFeedback.current

    val zIndex = if (index == dragScope.index) 10f else 0f
    var height by remember { mutableStateOf(0) }
    var originalY by remember { mutableStateOf(0f) }

    fun getOffset() =
        if (index == dragScope.index) dragScope.offset.roundToInt()
        else if (index > dragScope.index) {
            val targetY = originalY - dragScope.height
            val thresholdY = targetY + height - minOf(height, dragScope.height) / 2f
            if (dragScope.currentY > thresholdY) -dragScope.height
            else 0
        }
        else if (index < dragScope.index) {
            val thresholdY = originalY + minOf(height, dragScope.height) / 2f
            if (dragScope.currentY < thresholdY) dragScope.height
            else 0
        }
        else 0

    val offset =
        if (index != dragScope.index) {
            val targetOffset by remember { derivedStateOf { getOffset() } }
            animateIntAsState(targetValue = targetOffset).value
        }
        else getOffset()

    Modifier
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    dragScope.index = index
                    dragScope.height = height
                    dragScope.offset = 0f
                    dragScope.currentY = originalY
                },
                onDragEnd = {
                    dragScope.reset()
                },
                onDragCancel = {
                    dragScope.reset()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragScope.offset += dragAmount.y
                    dragScope.currentY += dragAmount.y
                }
            )
        }
}