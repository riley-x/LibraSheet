package com.example.librasheet.data.entity

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun categoryIdToString(id: CategoryId) = id.fullName

    @TypeConverter
    fun stringToCategoryId(id: String) = id.toCategoryId()
}