package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.librasheet.data.getYearAndMonthFromMonthEnd
import com.example.librasheet.data.thisMonthEnd
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.dataClasses.ImmutableList

class TimeRangeDialog: DialogHolder {
    override var isOpen by mutableStateOf(false)
        private set

    private var startYear = 0
    private var startMonth = 0
    private var endYear = 0
    private var endMonth = 0
    private var selectionStartYear = mutableStateOf(0)
    private var selectionStartMonth = mutableStateOf(0)
    private var selectionEndYear = mutableStateOf(0)
    private var selectionEndMonth = mutableStateOf(0)

    private var onSave: ((selectionStart: Int, selectionEnd: Int) -> Unit)? = null

    fun open(
        start: Int,
        end: Int,
        onSave: (selectionStart: Int, selectionEnd: Int) -> Unit
    ) {
        isOpen = true
        this.onSave = onSave

        val s = getYearAndMonthFromMonthEnd(start)
        startYear = s.first
        startMonth = s.second

        val e = getYearAndMonthFromMonthEnd(end)
        endYear = e.first
        endMonth = e.second
    }

    fun clear() {
        isOpen = false
        selectionStartYear.value = 0
        selectionStartMonth.value = 0
        selectionEndYear.value = 0
        selectionEndMonth.value = 0
    }

    private fun onSelected(year: Int, month: Int) {
        if (selectionEndYear.value != 0 || selectionStartYear.value == 0) {
            selectionStartYear.value = year
            selectionStartMonth.value = month
            selectionEndYear.value = 0
            selectionEndMonth.value = 0
        } else {
            selectionEndYear.value = year
            selectionEndMonth.value = month
        }
    }

    fun onClose(cancelled: Boolean) {
        if (cancelled) { clear() }
        else {
            if (selectionStartYear.value != 0 && selectionEndYear.value != 0) {
                val start = thisMonthEnd(selectionStartYear.value, selectionStartMonth.value)
                val end = thisMonthEnd(selectionEndYear.value, selectionEndMonth.value)
                onSave?.invoke(start, end)
            }
            clear()
        }
    }

    @Composable
    override fun Content() {
        if (isOpen) {
            TimeRangeDialogComposable(
                startYear = startYear,
                startMonth = startMonth,
                endYear = endYear,
                endMonth = endMonth,
                selectionStartYear = selectionStartYear.value,
                selectionStartMonth = selectionStartMonth.value,
                selectionEndYear = selectionEndYear.value,
                selectionEndMonth = selectionEndMonth.value,
                onClose = ::onClose,
                onSelected = ::onSelected,
            )
        }
    }
}


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
    onSelected: (year: Int, month: Int) -> Unit = { _, _ -> },
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
                        val month = row * monthsPerRow + col + 1 // 1-index here, but remember to 0-index in arrays
                        val cur = year * 100 + month
                        val valid = cur in start..end
                        val selected = cur == selectionStart || cur == selectionEnd
                        val highlighted = !selected && cur in selectionStart..selectionEnd

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (selected) MaterialTheme.colors.primary else Color.Unspecified,
                            modifier = Modifier
                                .clickable(valid) { onSelected(year, month) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = months.items[month - 1],
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
