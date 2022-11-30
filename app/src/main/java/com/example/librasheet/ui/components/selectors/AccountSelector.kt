package com.example.librasheet.ui.components.selectors

import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import com.example.librasheet.data.entity.Account
import com.example.librasheet.data.entity.Category
import com.example.librasheet.data.entity.isValid
import com.example.librasheet.ui.components.ColorIndicator
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewAccount
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewIncomeCategories2


@Composable
fun AccountSelector(
    selection: Account?,
    options: List<Account?>,
    modifier: Modifier = Modifier,
    onSelection: (Account?) -> Unit = { },
) {
    DropdownSelector(
        selection = selection,
        options = options,
        onSelection = onSelection,
        modifier = modifier,
    ) { it, _ ->
        ColorIndicator(it?.color ?: Color.Unspecified)

        Text(
            text = it?.name ?: "None",
            fontStyle = if (it != null) FontStyle.Normal else FontStyle.Italic,
            color = MaterialTheme.colors.onSurface.copy(
                alpha = if (it != null) ContentAlpha.high else ContentAlpha.medium,
            )
        )
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            AccountSelector(
                selection = previewAccounts[0],
                options = previewAccounts
            )
        }
    }
}