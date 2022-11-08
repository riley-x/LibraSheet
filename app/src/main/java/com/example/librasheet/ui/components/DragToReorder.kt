package com.example.librasheet.ui.components

import android.util.Log
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize

// See also https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1

class DragScope {
    var groupId by mutableStateOf(-1)
    var index by mutableStateOf(-1)

    var content by mutableStateOf<(@Composable (DragScope, Any?) -> Unit)?>(null)
    var size by mutableStateOf(IntSize.Zero)
    var originalPos by mutableStateOf(Offset.Zero)

    var offset by mutableStateOf(0f)

    fun reset() {
        groupId = -1
        index = -1
        content = null
        offset = 0f
        size = IntSize.Zero
        originalPos = Offset.Zero
    }
    fun isTarget(groupId: Int, index: Int) = groupId == this.groupId && index == this.index

    @Composable
    fun PlaceContent(state: Any? = null) {
        content?.invoke(this, state)
    }
}

val LocalDragScope = compositionLocalOf { DragScope() }

@Composable
fun DragToReorder(
    index: Int,
    groupId: Int,
    modifier: Modifier = Modifier,
    state: Any? = null,
    enabled: Boolean = true,
    content: @Composable (DragScope, Any?) -> Unit = { _, _ -> },
) {
    if (!enabled || index < 0) {
        content(DragScope(), state)
    } else {
        val haptic = LocalHapticFeedback.current
        val dragScope = LocalDragScope.current

        var size by remember { mutableStateOf(IntSize.Zero) }
        var originalPos by remember { mutableStateOf(Offset.Zero) }

        fun getOffset() =
            if (groupId != dragScope.groupId) 0
            else if (index > dragScope.index) {
                val targetY = originalPos.y - dragScope.size.height
                val thresholdY = targetY + size.height - minOf(size.height, dragScope.size.height) / 2f
                if (dragScope.originalPos.y + dragScope.offset > thresholdY) -dragScope.size.height
                else 0
            }
            else if (index < dragScope.index) {
                val thresholdY = originalPos.y + minOf(size.height, dragScope.size.height) / 2f
                if (dragScope.originalPos.y + dragScope.offset < thresholdY) dragScope.size.height
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
                size = it.size
                originalPos = it.localToRoot(Offset.Zero)
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
                            dragScope.size = size
                            dragScope.originalPos = originalPos
                            dragScope.content = content
                            dragScope.offset = 0f
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
                    }
                )
            }
        ) {
            content(dragScope, state)
        }
    }
}

