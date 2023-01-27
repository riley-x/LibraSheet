package com.example.librasheet.data

import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList

enum class Institution(
    override val displayName: String,
    val csvPattern: String,
    val dateFormat: String,
    val invertScreenReader: Boolean,
): HasDisplayName {
    BANK_OF_AMERICA(
        displayName = "Bank of America",
        csvPattern = "date,name,value",
        dateFormat = "MM/dd/yyyy",
        invertScreenReader = false,
    ),
    BANK_OF_AMERICA_CREDIT_CARD(
        displayName = "Bank of America (Credit Cards)",
        csvPattern = "date,,name,,value",
        dateFormat = "MM/dd/yyyy",
        invertScreenReader = true,
    ),
    VENMO(
        displayName = "Venmo",
        csvPattern = ",,date,,Complete,name,name,name,value,,,,,,Venmo balance",
        dateFormat = "yyyy-MM-dd",
        invertScreenReader = false,
    ),
    CHASE_CREDIT_CARD(
        displayName = "Chase (Credit Cards)",
        csvPattern = "date,,name,,,value,",
        dateFormat = "MM/dd/yyyy",
        invertScreenReader = true,
    ),
    UNKNOWN(
        displayName = "Unknown",
        csvPattern = "date,name,value",
        dateFormat = "MM/dd/yyyy",
        invertScreenReader = false,
    ),
}

val allInstitutions = ImmutableList(Institution.values().toList())