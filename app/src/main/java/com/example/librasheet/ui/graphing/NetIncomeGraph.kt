package com.example.librasheet.ui.graphing

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import com.example.librasheet.ui.components.NoDataError
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.*
import kotlin.math.roundToInt


data class NetIncomeGraphState(
    val axes: MutableState<AxesState> = mutableStateOf(AxesState()),
    val values1: SnapshotStateList<Double> = mutableStateListOf(),
    val values2: SnapshotStateList<Double> = mutableStateListOf(),
    val valuesNet: SnapshotStateList<Double> = mutableStateListOf(),
)

/**
 * Draws two series of bar charts for positive and negative values respectively, then a line for
 * their net sum.
 * The user x coordinates in this case are simply 0..lastIndex.
 *
 * @property lineColor Color of the net total line
 * @property aboveColor Color of the fill for values above 0
 * @property belowColor Color of the fill for values below 0
 * @property barWidth Width of each bar as a fraction [0, 1] of the distance between them
 */
@Composable
fun NetIncomeGraph(
    state: NetIncomeGraphState,
    modifier: Modifier = Modifier,
    onHover: (isHover: Boolean, loc: Int) -> Unit = { _, _ -> },
) {
    if (state.values1.isEmpty()) {
        NoDataError(modifier)
    } else {
        val hoverLoc = remember { mutableStateOf(-1) }
        val showHover by remember { derivedStateOf { hoverLoc.value >= 0 } }

        val graph1 = binaryBarGraphDrawer(
            values = state.values1,
            axisColor = Color.Transparent,
            aboveColor = MaterialTheme.colors.primary.copy(alpha = 0.3f),
            belowColor = MaterialTheme.colors.error.copy(alpha = 0.3f),
        )
        val graph2 = binaryBarGraphDrawer(
            values = state.values2,
            axisColor = Color.Transparent,
            aboveColor = MaterialTheme.colors.primary.copy(alpha = 0.3f),
            belowColor = MaterialTheme.colors.error.copy(alpha = 0.3f),
        )
        val graphNet = lineGraphDrawer(values = state.valuesNet)
        val graphHover = discreteHover(loc = hoverLoc)

        fun onHoverInner(isHover: Boolean, x: Float, y: Float) {
            if (state.values1.isEmpty()) return
            if (isHover) {
                hoverLoc.value = MathUtils.clamp(x.roundToInt(), 0, state.values1.lastIndex)
            } else {
                hoverLoc.value = -1
            }
            onHover(isHover, hoverLoc.value)
        }

        Graph(
            axesState = state.axes,
            contentBefore = {
                graph1(it)
                graph2(it)
                graphNet(it)
            },
            contentAfter = { if (showHover) graphHover(it) },
            onHover = ::onHoverInner,
            modifier = modifier,
        )
    }
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            NetIncomeGraph(
                state = previewNetIncomeState,
                modifier = Modifier.size(360.dp)
            )
        }
    }
}
