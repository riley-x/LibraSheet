package com.example.librasheet.ui.components.selectors

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.isValid
import com.example.librasheet.ui.components.ColorIndicator
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewIncomeCategories2


@Composable
fun CategorySelector(
    selection: Category?,
    options: List<Category>,
    modifier: Modifier = Modifier,
    onSelection: (Category?) -> Unit = { },
) {
    DropdownSelector(
        selection = selection,
        options = options,
        onSelection = onSelection,
        modifier = modifier,
    ) {
        ColorIndicator(it?.color ?: Color.Unspecified)
        Text(
            text = it?.id?.name?.ifEmpty { "None" } ?: "None",
            fontStyle = if (it.isValid()) FontStyle.Normal else FontStyle.Italic,
            color = MaterialTheme.colors.onSurface.copy(
                alpha = if (it.isValid()) ContentAlpha.high else ContentAlpha.medium,
            )
        )
    }
}


@Preview
@Composable
private fun PreviewCustom() {
    LibraSheetTheme {
        Surface {
            CategorySelector(
                selection = previewIncomeCategories2[0],
                options = previewIncomeCategories2
            )
        }
    }
}