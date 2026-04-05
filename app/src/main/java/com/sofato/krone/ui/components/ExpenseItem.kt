package com.sofato.krone.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    homeCurrency: Currency? = null,
) {
    val isForeign = homeCurrency != null && expense.currency.code != homeCurrency.code

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(
            iconName = expense.category.iconName,
            colorHex = expense.category.colorHex,
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = expense.note?.takeIf { it.isNotBlank() } ?: expense.category.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (expense.note?.isNotBlank() == true) {
                Text(
                    text = expense.category.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyFormatter.formatDisplay(expense.amount, expense.currency),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End,
            )
            if (isForeign && expense.homeAmount > 0) {
                Text(
                    text = "\u2248 ${CurrencyFormatter.formatDisplay(expense.homeAmount, homeCurrency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
