package com.example.librasheet.data

import com.example.librasheet.viewModel.dataClasses.HasDisplayName

enum class Institution(
    override val displayName: String
): HasDisplayName {
    BANK_OF_AMERICA("Bank of America")
}