package com.example.librasheet.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.CategoryRule
import com.example.librasheet.ui.components.ColorIndicator
import com.example.librasheet.ui.components.libraRow
import com.example.librasheet.ui.components.libraRowHorizontalPadding
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewRules

@Composable
fun CategoryRuleRow(
    rule: CategoryRule,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .libraRow(horizontalPadding = 0.dp)
                .padding(start = libraRowHorizontalPadding)
        ) {
            Text(
                text = rule.pattern,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                modifier = Modifier.weight(10f)
            )

            Spacer(Modifier.width(12.dp))
            ColorIndicator(rule.category?.color ?: Color.Unspecified)

            val special = (rule.category == null) || rule.category.key <= 0
            Text(
                text = rule.category?.id?.name ?: "None",
                fontStyle = if (special) FontStyle.Italic else FontStyle.Normal,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .weight(8f)
                    .alpha(if (special) ContentAlpha.disabled else ContentAlpha.high)
            )
        }
    }
}


@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        CategoryRuleRow(rule = previewRules[0])
    }
}