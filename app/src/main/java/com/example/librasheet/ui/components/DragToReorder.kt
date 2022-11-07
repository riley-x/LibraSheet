package com.example.librasheet.ui.components

import android.util.Log
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

// See also https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1

class DragScope {
    var groupId by mutableStateOf(-1)
    var index by mutableStateOf(-1)
    var content by mutableStateOf<(@Composable () -> Unit)?>(null)
    var height by mutableStateOf(0)

    var offset by mutableStateOf(0f)
    var currentY by mutableStateOf(0f)

    fun reset() {
        groupId = -1
        index = -1
        content = null
        height = 0
        offset = 0f
        currentY = 0f
    }
    fun isTarget(groupId: Int, index: Int) = groupId == this.groupId && index == this.index
}

val LocalDragScope = compositionLocalOf { DragScope() }

@Composable
fun DragToReorder(
    index: Int,
    groupId: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = { },
) {
    val haptic = LocalHapticFeedback.current
    val dragScope = LocalDragScope.current

    var height by remember { mutableStateOf(0) }
    var originalY by remember { mutableStateOf(0f) }

    fun getOffset() =
        if (groupId != dragScope.groupId) 0
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

    val targetOffset by remember { derivedStateOf { getOffset() } }
    val offset = animateIntAsState(targetValue = targetOffset).value
    val alpha by remember { derivedStateOf {
        if (dragScope.isTarget(groupId, index)) 0.3f else 1f
    } }

    Box(modifier = modifier
        .onGloballyPositioned {
            height = it.size.height
            originalY = it.localToRoot(Offset.Zero).y
        }
        .alpha(alpha)
        .drawWithContent { // For some reason Modifier.offset() was being really buggy...
            translate(top = offset.toFloat()) {
                this@drawWithContent.drawContent()
            }
        }
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    if (dragScope.index == -1) {
                        Log.d("Libra/DragToReorder", "drag start: $groupId $index")
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dragScope.groupId = groupId
                        dragScope.index = index
                        dragScope.height = height
                        dragScope.content = content
                        dragScope.offset = 0f
                        dragScope.currentY = originalY
                    }
                },
                onDragEnd = {
                    Log.d("Libra/DragToReorder", "drag end: $groupId $index")
                    dragScope.reset()
                },
                onDragCancel = {
                    Log.d("Libra/DragToReorder", "drag cancel: $groupId $index")
                    if (dragScope.isTarget(groupId, index)) dragScope.reset()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragScope.offset += dragAmount.y
                    dragScope.currentY += dragAmount.y
                }
            )
        }
    ) {
        content()
    }
}

