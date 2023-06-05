package com.example.librasheet.ui.graphing

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path


fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)
fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)

data class DiscreteGraphState(
    val axes: MutableState<AxesState> = mutableStateOf(AxesState()),
    val values: SnapshotStateList<Double> = mutableStateListOf(),
)