package com.sofato.krone.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun AmountText(
    amountMinor: Long,
    currency: Currency,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    isConverted: Boolean = false,
) {
    val formatted = CurrencyFormatter.formatDisplay(amountMinor, currency)
    val displayText = if (isConverted) "≈ $formatted" else formatted

    Text(
        text = displayText,
        style = style,
        modifier = modifier,
    )
}
