package com.example.librasheet.ui.transaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.librasheet.data.entity.*
import com.example.librasheet.ui.components.ColorCodedRow
import com.example.librasheet.ui.components.formatDateIntSimple
import com.example.librasheet.ui.components.formatDollar
import com.example.librasheet.ui.components.libraRowHorizontalPadding
import com.example.librasheet.ui.theme.LibraSheetTheme
import com.example.librasheet.viewModel.preview.previewAccounts
import com.example.librasheet.viewModel.preview.previewAllocations
import com.example.librasheet.viewModel.preview.previewReimbursements
import com.example.librasheet.viewModel.preview.previewTransactions
import kotlin.collections.find

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    modifier: Modifier = Modifier,
) {
    ColorCodedRow(color = transaction.category.color, modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(10f)
                .padding(end = 10.dp)
        ) {
            Text(
                text = transaction.name,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${transaction.category.id.name}, ${transaction.accountName}",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatDollar(transaction.valueAfterReimbursements),
                color =
                    if (transaction.value < 0) MaterialTheme.colors.error
                    else MaterialTheme.colors.primary,
                fontStyle =
                    if (transaction.value != transaction.valueAfterReimbursements) FontStyle.Italic
                    else FontStyle.Normal,
            )
            Text(
                text = formatDateIntSimple(transaction.date),
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                style = MaterialTheme.typography.body2,
            )
        }
    }
}




@Composable
fun ReimbursementRow(
    r: ReimbursementWithValue,
    accounts: SnapshotStateList<Account>,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = { },
) {
    val account = accounts.find(r.transaction.accountKey)
    ColorCodedRow(
        color = account?.color ?: Color.Unspecified,
        horizontalPadding = 0.dp,
        modifier = modifier.padding(start = libraRowHorizontalPadding),
    ) {
        Column(
            modifier = Modifier
                .weight(10f)
                .padding(end = 10.dp)
        ) {
            Text(
                text = r.transaction.name,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatDateIntSimple(r.transaction.date) + ", " + (account?.name ?: ""),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                maxLines = 1,
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatDollar(r.value),
            )
            Text(
                text = formatDollar(r.transaction.value),
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                style = MaterialTheme.typography.body2,
            )
        }
        content()
    }
}


@Composable
fun AllocationRow(
    allocation: Allocation,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = { },
) {
    ColorCodedRow(
        color = allocation.category.color,
        horizontalPadding = 0.dp,
        modifier = modifier.padding(start = libraRowHorizontalPadding),
    ) {
        Column(
            modifier = Modifier
                .weight(10f)
                .padding(end = 10.dp)
        ) {
            Text(
                text = allocation.name,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = allocation.category.id.name,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                maxLines = 1,
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatDollar(allocation.value),
            )
        }
        content()
    }
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Surface {
            TransactionRow(transaction = previewTransactions[0])
        }
    }
}


@Preview
@Composable
private fun PreviewReimbursement() {
    LibraSheetTheme {
        Surface {
            ReimbursementRow(
                r = previewReimbursements[0],
                accounts = previewAccounts
            )
        }
    }
}

@Preview
@Composable
private fun PreviewAllocation() {
    LibraSheetTheme {
        Surface {
            AllocationRow(previewAllocations[0])
        }
    }
}