package com.example.librasheet.data.database

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun colorToInt(color: Color) = color.toArgb()

    @TypeConverter
    fun intToColor(int: Int) = Color(int)

    @TypeConverter
    fun categoryIdToString(id: CategoryId) = id.fullName

    @TypeConverter
    fun stringToCategoryId(id: String) = id.toCategoryId()
}