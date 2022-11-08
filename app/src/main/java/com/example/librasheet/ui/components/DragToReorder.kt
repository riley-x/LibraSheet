package com.example.librasheet.ui.components

import android.util.Log
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * This file contains utilities to declare composables that can be dragged vertically to reorder
 * them.
 *
 * Since you might want to drag components from any level of hierarchy in the composition graph, the
 * current drag state is declared via a CompositionLocal [DragScope], which can be accessed as
 * `val dragScope = LocalDragScope.current`.
 *
 * The [DragScope] records the current element being dragged, its composition, and the current drag
 * offset. An element is declared draggable by wrapping it in [DragToReorderTarget]. It should be
 * uniquely identifiable across the entire [DragScope] by its [groupId] and [index] inside the group.
 * The [groupId] allows there to be multiple groups of lists that can be reordered independently.
 *
 * [DragToReorderTarget] assumes that elements are vertically arranged and tightly packed, and
 * calculates the reordering based on the y positions of the dragged element and the other targets.
 * Note that during the drag, the elements are not reordered but only visually shifted.
 *
 * The [DragHost] is the parent that owns the drag, and provides its children with a local scope.
 * All composables that should go below (in the z direction) a current dragged item need to be
 * contained inside the host. When an element is dragged, the [DragHost] simply turns the original
 * to 0 alpha and draws a new copy on top. If the drawn element has state, the copy will need to
 * have the same state. The state of each target should be hoisted and passed to [DragToReorderTarget].
 *
 * See also https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1
 */

class DragScope {
    /** Identifiers **/
    var groupId by mutableStateOf(-1)
    var index by mutableStateOf(-1)
    /** Composition **/
    var content by mutableStateOf<(@Composable (DragScope, Any?) -> Unit)?>(null)
    var contentState by mutableStateOf<Any?>(null)
    var size by mutableStateOf(IntSize.Zero)
    var originalPos by mutableStateOf(Offset.Zero)
    /** Drag **/
    var offset by mutableStateOf(0f)

    fun reset() {
        /** Identifiers **/
        groupId = -1
        index = -1
        /** Composition **/
        content = null
        contentState = null
        size = IntSize.Zero
        originalPos = Offset.Zero
        /** Drag **/
        offset = 0f
    }

    fun isTarget(groupId: Int, index: Int) = groupId == this.groupId && index == this.index

    @Composable
    fun PlaceContent() {
        content?.invoke(this, contentState)
    }
}

val LocalDragScope = compositionLocalOf { DragScope() }

@Composable
fun DragToReorderTarget(
    index: Int,
    groupId: Int,
    modifier: Modifier = Modifier,
    contentState: Any? = null,
    enabled: Boolean = true,
    content: @Composable (DragScope, Any?) -> Unit = { _, _ -> },
) {
    if (!enabled || index < 0) {
        content(DragScope(), contentState)
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
                if (dragScope.originalPos.y + dragScope.offset > thresholdY) - dragScope.size.height
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
            if (dragScope.isTarget(groupId, index)) 0f else 1f
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
            /** Pay attention to the key. This effectively launches a coroutine which runs the
             * block. But this means the [contentState] parameter will keep it's value when the
             * coroutine was launched. Setting the key to it makes sure the coroutine is cancelled
             * and relaunched when the state changes.
             **/
            .pointerInput(contentState) {
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
                            dragScope.contentState = contentState
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
            content(dragScope, contentState)
        }
    }
}


@Composable
fun DragHost(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var startPos by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier.onGloballyPositioned { startPos = it.localToRoot(Offset.Zero) }
    ) {
        val dragScope = remember { DragScope() } // Don't need state here since this is only passed into other composables.

        CompositionLocalProvider(
            LocalDragScope provides dragScope
        ) {
            content()

            Box(modifier = Modifier
                .size(dragScope.size.toDpSize())
                .offset { IntOffset(
                        (dragScope.originalPos.x - startPos.x).roundToInt(),
                        (dragScope.offset + dragScope.originalPos.y - startPos.y).roundToInt()
                ) }
            ) {
                dragScope.PlaceContent()
            }
        }
    }
}

