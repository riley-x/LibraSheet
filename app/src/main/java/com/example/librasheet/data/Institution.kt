package com.example.librasheet.data

import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList

enum class Institution(
    override val displayName: String,
    val csvPattern: String,
    val dateFormat: String,
): HasDisplayName {
    BANK_OF_AMERICA("Bank of America", "date,name,value", "MM/dd/yyyy"),
    BANK_OF_AMERICA_CREDIT_CARD("Bank of America (Credit Cards)", "date,,name,,value", "MM/dd/yyyy"),
    VENMO("Venmo", ",,date,,Complete,name,name,name,value,,,,,,Venmo balance", "yyyy-MM-dd"),
    CHASE("Chase", ",date,name,,,value,", "MM/dd/yyyy"),
    UNKNOWN("Unknown", "date,name,value", "MM/dd/yyyy"),
}

val allInstitutions = ImmutableList(Institution.values().toList())