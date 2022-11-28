package com.example.librasheet.data.entity

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.librasheet.data.Institution
import com.example.librasheet.data.Series
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue

const val accountTable = "account"

@Immutable
@Entity(
    tableName = accountTable
)
data class Account(
    @PrimaryKey(autoGenerate = true) override val key: Long,
    @NonNull override val name: String,
    val institution: Institution,
    @NonNull @ColumnInfo(defaultValue = "") val csvPattern: String,
    val colorLong: Long,
    val listIndex: Int,
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
        csvPattern: String,
        colorLong: Long,
        listIndex: Int,
    ) : this(
        key = key,
        name = name,
        institution = institution,
        csvPattern = csvPattern,
        colorLong = colorLong,
        listIndex = listIndex,
        balance = 0,
    )

    /** Normal constructor **/
    constructor(
        name: String,
        color: Color,
        institution: Institution = Institution.UNKNOWN,
        csvPattern: String = "",
        key: Long = 0,
        listIndex: Int = -1,
        balance: Long = 0,
    ) : this(
        key = key,
        name = name,
        institution = institution,
        csvPattern = csvPattern,
        colorLong = color.value.toLong(),
        listIndex = listIndex,
        balance = balance,
    )
}


fun List<Account>.find(key: Long?) = if (key == null) null else find { it.key == key }