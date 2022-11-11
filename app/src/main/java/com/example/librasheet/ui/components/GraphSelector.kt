package com.example.librasheet.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.librasheet.viewModel.dataClasses.ImmutableList

/**
 * This allows the user to switch between different visuals using a dial selector. The switching is
 * animated as a sliding transition matching the dial direction.
 *
 * @param content The content for each tab needs to be defined here. This should usually be a when
 * case on the passed argument, which is the target tab index. DO NOT use [selectedTab].
 * @param modifier Note this is passed to the AnimatedContent, and doesn't affect the dial selector
 * or the containing column.
 */
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun GraphSelector(
    tabs: ImmutableList<String>,
    selectedTab: State<Int>,
    modifier: Modifier = Modifier,
    onSelection: (Int) -> Unit = { },
    content: @Composable AnimatedVisibilityScope.(Int) -> Unit = { },
) {
    /** This needs to be passed into AnimatedContent as part of the state so the animation knows
     * which direction to move (which CAN'T be figured out with just the indices when there are exactly
     * two tabs). However, the hoisted states don't need to care about it. **/
    // TODO do we need to hoist the selected tab state at all?
    val wasFromDialRight = remember { mutableStateOf(true) }
    val swipeableState = rememberSwipeableState(0,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = 5f,
            // spring starts fast but ends slowly. This makes the animation short circuit a little
            // at the end, which speeds up the transition of the graphic.
        )
    )

    Column {
        AnimatedContent(
            targetState = Pair(selectedTab.value, wasFromDialRight.value),
            transitionSpec = {
                val towards =
                    if (targetState.second) AnimatedContentScope.SlideDirection.Start
                    else AnimatedContentScope.SlideDirection.End
                slideIntoContainer(
                    towards = towards,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                    )
                ) + fadeIn(animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                )) with
                        slideOutOfContainer(
                            towards = towards,
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntOffset.VisibilityThreshold,
                            )) + fadeOut()
            },
            content = { content(it.first) },
            modifier = modifier,
        )

        DialSelector(
            labels = tabs,
            swipeableState = swipeableState,
            onSelection = { index, loop ->
                // This if check is important because when loop is true, the launched effect in the
                // dial selector will call onSelection again with loop = false
                if (index == selectedTab.value) return@DialSelector

                val greater = index > selectedTab.value
                wasFromDialRight.value = if (loop) !greater else greater
                onSelection(index)
            },
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
        )
    }
}