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
 * them. Read in the order of the classes.
 *
 * Simple usage:
 * ```
 *  DragHost {
 *      LazyColumn {
 *          itemsIndexed(myList) { index, item ->
 *              DragToReorderTarget(
 *                  index = index,
 *                  groupId = 0,
 *                  onDragEnd = { },
 *              ) {
 *                  Text(item)
 *              }
 *          }
 *      }
 *  }
 * ```
 *
 * See also https://blog.canopas.com/android-drag-and-drop-ui-element-in-jetpack-compose-14922073b3f1
 */

/**
 * Since you might want to drag components from any level of hierarchy in the composition graph, the
 * current drag state is declared via a CompositionLocal, which can be accessed as
 * `val dragScope = LocalDragScope.current`.
 *
 * [DragScope] records the current element being dragged, its composition, and the current drag
 * offset. An element is declared draggable by wrapping it in [DragToReorderTarget]. It should be
 * uniquely identifiable across the entire [DragScope] by its [groupId] and [index] inside the group.
 * The [groupId] allows there to be multiple groups of lists that can be reordered independently.
 */
class DragScope {
    /** Identifiers **/
    var groupId by mutableStateOf(-1)
    var index by mutableStateOf(-1)
    /** Composition **/
    var content by mutableStateOf<(@Composable (DragScope, Any?) -> Unit)?>(null)
    var contentState by mutableStateOf<Any?>(null)
    var contentSize by mutableStateOf(IntSize.Zero)
    var originalPos by mutableStateOf(Offset.Zero)
    /** Drag **/
    var offset by mutableStateOf(0f)
    var affectedIndices = mutableSetOf<Int>() // this is used ONLY for onDragFinish. No composable uses this so it doesn't need to be a state

    fun reset() {
        /** Identifiers **/
        groupId = -1
        index = -1
        /** Composition **/
        content = null
        contentState = null
        contentSize = IntSize.Zero
        originalPos = Offset.Zero
        /** Drag **/
        offset = 0f
        affectedIndices.clear()
    }

    fun isTarget(groupId: Int, index: Int) = groupId == this.groupId && index == this.index

    @Composable
    fun PlaceContent() {
        content?.invoke(this, contentState)
    }
}

val LocalDragScope = compositionLocalOf { DragScope() }


/**
 * This wrapper composable registers its content to be a reorder target. This means it is both a
 * draggable item and a drag destination. When it's the draggable item, the content is hidden and
 * left to [DragHost] to draw. When it's a destination, a shift offset is calculated based on the
 * current dragged item's position. Note that during the drag, the elements are not reordered but
 * only visually shifted.
 *
 * WARNING: This class assumes that elements are vertically arranged and tightly packed, and
 * calculates the reordering based on the y positions of the targets.
 *
 * @param index Should uniquely identify the target in its group (usually the list index). It should
 * always be non-negative.
 * @param groupId Should uniquely identify the group this target belongs in. Targets can only be
 * reordered within the same group.
 * @param contentState Should include all state information necessary to reproduce [content].
 * [DragHost] will use this state when drawing the hovering dragged item.
 * @param onDragEnd Returns the current dragged element id and the final index it was dropped at.
 * Note that this callback will only be triggered once, by the dragged element.
 */
@Composable
fun DragToReorderTarget(
    index: Int,
    groupId: Int,
    modifier: Modifier = Modifier,
    contentState: Any? = null,
    enabled: Boolean = true,
    onDragEnd: (groupId: Int, startIndex: Int, endIndex: Int) -> Unit = { _, _, _ -> },
    content: @Composable (DragScope, Any?) -> Unit = { _, _ -> },
) {
    if (!enabled || index < 0) {
        content(DragScope(), contentState)
    } else {
        val haptic = LocalHapticFeedback.current
        val dragScope = LocalDragScope.current

        var size by remember { mutableStateOf(IntSize.Zero) }
        var originalPos by remember { mutableStateOf(Offset.Zero) }

        /** When list items are tightly packed, reordering merely shifts the items between
         * [startDragIndex, currentDragPosition]. Each target calculates for itself whether it is in
         * this range, and shifts itself if it is. The shift is exactly the height of the dragged
         * item. Note that we don't apply any offset to the dragged item, and hide it instead. It'll
         * be drawn by the [DragHost] at highest z-order.
         */
        fun getOffset() =
            if (groupId != dragScope.groupId) 0
            else if (index > dragScope.index) {
                val targetY = originalPos.y - dragScope.contentSize.height
                val thresholdY = targetY + size.height - minOf(size.height, dragScope.contentSize.height) / 2f
                if (dragScope.originalPos.y + dragScope.offset > thresholdY) - dragScope.contentSize.height
                else 0
            }
            else if (index < dragScope.index) {
                val thresholdY = originalPos.y + minOf(size.height, dragScope.contentSize.height) / 2f
                if (dragScope.originalPos.y + dragScope.offset < thresholdY) dragScope.contentSize.height
                else 0
            }
            else 0

        val targetOffset by remember { derivedStateOf { getOffset() } }
        val offset = animateIntAsState(targetValue = targetOffset).value

        if (groupId == dragScope.groupId) {
            LaunchedEffect(targetOffset) {
                if (targetOffset != 0) dragScope.affectedIndices.add(index)
                else dragScope.affectedIndices.remove(index)
            }
        }

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
                        /** When there are nested elements that can both be dragged, both will trigger
                         * onDragStart but one will be canceled right away, thanks to(?) change.consume().
                         * So need to check that we're in the right drag.
                         */
                        if (dragScope.index == -1) {
                            Log.d("Libra/DragToReorder", "drag start: $groupId $index")
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            dragScope.groupId = groupId
                            dragScope.index = index
                            dragScope.contentSize = size
                            dragScope.originalPos = originalPos
                            dragScope.content = content
                            dragScope.contentState = contentState
                            dragScope.offset = 0f
                        }
                    },
                    onDragEnd = {
                        val endIndex = if (dragScope.affectedIndices.isEmpty()) index
                            else if (dragScope.affectedIndices.first() < index) dragScope.affectedIndices.min()
                            else dragScope.affectedIndices.max()
                        Log.d("Libra/DragToReorder", "drag end: $groupId $index $endIndex")
                        dragScope.reset()
                        onDragEnd(groupId, index, endIndex)
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

/**
 * The [DragHost] is the parent that owns the drag, and provides its children with a local scope.
 * All composables that should go below (in the z direction) a current dragged item need to be
 * contained inside the host. When an element is dragged, the [DragToReorderTarget] simply turns the
 * original to 0 alpha. The [DragHost] then draws a new copy on top. If the drawn element has state,
 * the copy will need to have the same state. The state of each target should be hoisted and passed
 * to [DragToReorderTarget].
 */
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
                .size(dragScope.contentSize.toDpSize())
                .offset {
                    IntOffset(
                        (dragScope.originalPos.x - startPos.x).roundToInt(),
                        (dragScope.offset + dragScope.originalPos.y - startPos.y).roundToInt()
                    )
                }
            ) {
                dragScope.PlaceContent()
            }
        }
    }
}

