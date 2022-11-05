package com.example.librasheet.ui.graphing

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path


fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)
fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)

@Immutable
data class DiscreteGraphState(
    val axes: State<AxesState>,
    val values: SnapshotStateList<Float>,
)