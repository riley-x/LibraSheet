package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.librasheet.data.*
import com.example.librasheet.data.entity.Allocation
import com.example.librasheet.data.entity.Category
import com.example.librasheet.ui.components.selectors.CategorySelector
import com.example.librasheet.ui.components.textFields.textFieldBorder
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.LibraViewModel
import com.example.librasheet.viewModel.dataClasses.ImmutableList
import com.example.librasheet.viewModel.preview.previewCategory2
import com.example.librasheet.viewModel.preview.previewIncomeCategories2

//class TimeRangeDialog(
//    private val viewModel: LibraViewModel,
//): DialogHolder {
//    override var isOpen by mutableStateOf(false)
//        private set
//
//    private var isIncome = false
//    private var errorMessage by mutableStateOf("")
//    private var name = mutableStateOf("")
//    private var value = mutableStateOf("")
//    private var category = mutableStateOf(Category.None)
//
//    private var onSave: ((String, Long, Category?) -> Unit)? = null
//
//    fun open(
//        isIncome: Boolean,
//        allocation: Allocation? = null,
//        onSave: (String, Long, Category?) -> Unit
//    ) {
//        isOpen = true
//        name.value = allocation?.name ?: ""
//        value.value = allocation?.value?.toFloatDollar()?.toString() ?: ""
//        category.value = allocation?.category ?: Category.None
//        this.isIncome = isIncome
//        this.onSave = onSave
//    }
//
//    fun clear() {
//        isOpen = false
//        errorMessage = ""
//    }
//
//    fun onClose(cancelled: Boolean) {
//        if (cancelled) { clear() }
//        else {
//            val valueLong = value.value.toDoubleOrNull()?.toLongDollar()
//            if (valueLong == null) {
//                errorMessage = "Couldn't parse value"
//            } else if (valueLong < 0) {
//                errorMessage = "Value should be positive"
//            } else {
//                onSave?.invoke(name.value, valueLong, category.value)
//                clear()
//            }
//        }
//    }
//
//    @Composable
//    override fun Content() {
//        if (isOpen) {
//            AllocationDialogComposable(
//                name = name,
//                value = value,
//                category = category,
//                categories = if (isIncome) viewModel.categories.incomeTargets else viewModel.categories.expenseTargets,
//                onClose = ::onClose,
//            )
//        }
//    }
//}


private val months = ImmutableList(listOf(
    "Jan", "Feb", "Mar",
    "Apr", "May", "Jun",
    "Jul", "Aug", "Sep",
    "Oct", "Nov", "Dec",
))
private const val monthsPerRow = 4

@Composable
private fun TimeRangeDialogComposable(
    startYear: Int,
    startMonth: Int,
    endYear: Int, // inclusive!
    endMonth: Int, // inclusive!
    selectionStartYear: Int,
    selectionStartMonth: Int,
    selectionEndYear: Int, // inclusive!
    selectionEndMonth: Int, // inclusive!
    onClose: (cancelled: Boolean) -> Unit = { },
) {
    val start = startYear * 100 + startMonth
    val end = endYear * 100 + endMonth
    val selectionStart = selectionStartYear * 100 + selectionStartMonth
    val selectionEnd = selectionEndYear * 100 + selectionEndMonth

    Dialog(
        onCancel = { onClose(true) },
        onOk =  { onClose(false) },
        scrollable = true,
    ) {
        for (year in startYear..endYear) {
            Text(
                text = "$year",
                style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 6.dp)
            )
            for (row in 0 until 12/monthsPerRow) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (col in 0 until monthsPerRow) {
                        val month = row * monthsPerRow + col
                        val cur = year * 100 + month + 1
                        val valid = cur in start..end
                        val selected = cur == selectionStart || cur == selectionEnd
                        val highlighted = !selected && cur in selectionStart..selectionEnd

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (selected) MaterialTheme.colors.primary else Color.Unspecified,
                            modifier = Modifier
                                .clickable(valid) { }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = months.items[month],
                                    style = MaterialTheme.typography.subtitle2,
                                    color = if (highlighted) MaterialTheme.colors.primary
                                        else if (!valid) MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                                        else Color.Unspecified,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        TimeRangeDialogComposable(
            startYear = 2021,
            startMonth = 6,
            endYear = 2023,
            endMonth = 2,
            selectionStartYear = 2021,
            selectionStartMonth = 10,
            selectionEndYear = 2022,
            selectionEndMonth = 9,
        )
    }
}
