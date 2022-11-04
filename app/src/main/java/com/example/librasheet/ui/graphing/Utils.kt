package com.example.librasheet.ui.graphing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path


fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)
fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)