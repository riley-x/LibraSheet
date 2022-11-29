package com.example.librasheet.data

import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList

enum class Institution(
    override val displayName: String,
    val csvPattern: String,
    val dateFormat: String,
): HasDisplayName {
    BANK_OF_AMERICA(
        displayName = "Bank of America",
        csvPattern = "date,name,value",
        dateFormat = "MM/dd/yyyy"
    ),
    BANK_OF_AMERICA_CREDIT_CARD(
        displayName = "Bank of America (Credit Cards)",
        csvPattern = "date,,name,,value",
        dateFormat = "MM/dd/yyyy"
    ),
    VENMO(
        displayName = "Venmo",
        csvPattern = ",,date,,Complete,name,name,name,value,,,,,,Venmo balance",
        dateFormat = "yyyy-MM-dd"
    ),
    CHASE(
        displayName = "Chase",
        csvPattern = ",date,name,,,value,",
        dateFormat = "MM/dd/yyyy"
    ),
    UNKNOWN(
        displayName = "Unknown",
        csvPattern = "date,name,value",
        dateFormat = "MM/dd/yyyy"
    ),
}

val allInstitutions = ImmutableList(Institution.values().toList())