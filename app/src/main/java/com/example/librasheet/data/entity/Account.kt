@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
// So that we can access the inlined value class Color -> ULong -> Long

package com.example.librasheet.data.entity

import androidx.annotation.NonNull
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.librasheet.data.Institution
import com.example.librasheet.data.toFloatDollar
import com.example.librasheet.ui.graphing.PieChartValue

const val accountTable = "account"
const val accountHistoryTable = "account_history"

@Entity(
    tableName = accountTable
)
data class Account(
    @PrimaryKey(autoGenerate = true) val key: Long,
    @NonNull override val name: String,
    val institution: Institution,
    val colorLong: Long,
    val listIndex: Int,
    @Ignore val balance: Long,
): PieChartValue {
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
        institution: Institution = Institution.UNKNOWN,
        color: Color,
        listIndex: Int = -1,
    ) : this(
        key = 0,
        name = name,
        institution = institution,
        colorLong = color.value.data,
        listIndex = listIndex,
    )
}


@Entity(
    tableName = accountHistoryTable,
    primaryKeys = ["accountKey", "date"],
)
data class AccountHistory(
    val accountKey: Long,
    val date: Int,
    val balance: Long,
)