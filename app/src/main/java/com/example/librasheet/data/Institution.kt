package com.example.librasheet.data

import com.example.librasheet.viewModel.dataClasses.HasDisplayName
import com.example.librasheet.viewModel.dataClasses.ImmutableList

enum class Institution(
    override val displayName: String
): HasDisplayName {
    BANK_OF_AMERICA("Bank of America"),
    BANK_OF_AMERICA_CREDIT_CARD("Bank of America (Credit Cards)"),
    VENMO("Venmo"),
    CHASE("Chase"),
    UNKNOWN("Unknown"),
}

val allInstitutions = ImmutableList(Institution.values().toList())