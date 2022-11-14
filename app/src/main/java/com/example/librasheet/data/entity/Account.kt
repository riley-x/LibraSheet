package com.example.librasheet.data.entity

import androidx.annotation.NonNull
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.librasheet.data.Institution
import com.example.librasheet.data.Series
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue

const val accountTable = "account"

@Entity(
    tableName = accountTable
)
data class Account(
    @PrimaryKey(autoGenerate = true) override val key: Long,
    @NonNull override val name: String,
    val institution: Institution,
    val colorLong: Long,
    var listIndex: Int, // this is not used by compose, so safe to be a var
    val balance: Long,
): PieChartValue, Series {
    override val color: Color
        get() = Color(value = colorLong.toULong())
    override val value: Float
        get() = balance.toFloatDollar()

    /** Room constructor **/
    constructor(
        key: Long,
        name: String,
        institution: Institution,
        colorLong: Long,
        listIndex: Int,
    ) : this(
        key = key,
        name = name,
        institution = institution,
        colorLong = colorLong,
        listIndex = listIndex,
        balance = 0,
    )

    /** Normal constructor **/
    constructor(
        name: String,
        color: Color,
        institution: Institution = Institution.UNKNOWN,
        key: Long = 0,
        listIndex: Int = -1,
        balance: Long = 0,
    ) : this(
        key = key,
        name = name,
        institution = institution,
        colorLong = color.value.toLong(),
        listIndex = listIndex,
        balance = balance,
    )
}

