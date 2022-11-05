package com.example.librasheet.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * This allows the user to switch between different visuals using a dial selector. The switching is
 * animated as a sliding transition matching the dial direction.
 *
 * @param content The content for each tab needs to be defined here. This should usually be a when
 * case on the passed argument, which is the target tab index. DO NOT use [selectedTab].
 * @param modifier Note this is passed to the AnimatedContent, and doesn't affect the dial selector
 * or the containing column.
 */
@OptIn(ExperimentalAnimationApi::class)
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

    Column {
        AnimatedContent(
            targetState = Pair(selectedTab.value, wasFromDialRight.value),
            transitionSpec = {
                val towards =
                    if (targetState.second) AnimatedContentScope.SlideDirection.Start
                    else AnimatedContentScope.SlideDirection.End
                slideIntoContainer(
                    towards = towards,
                    animationSpec = tween(400 )
                ) + fadeIn(animationSpec = tween(400)) with
                        slideOutOfContainer(
                            towards = towards,
                            animationSpec = tween(200)
                        ) + fadeOut(animationSpec = tween(200))
            },
            content = { content(it.first) },
            modifier = modifier,
        )

        DialSelector(
            selected = selectedTab,
            labels = tabs,
            onSelection = { index, dialRight ->
                wasFromDialRight.value = dialRight
                onSelection(index)
            },
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
        )
    }
}