package com.sofato.krone.ui.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun BudgetOverviewCard(
    totalIncome: Long,
    totalFixed: Long,
    totalSavings: Long,
    discretionary: Long,
    currency: Currency,
    modifier: Modifier = Modifier,
) {
    val fixedColor = MaterialTheme.colorScheme.error
    val savingsColor = MaterialTheme.colorScheme.tertiary
    val discretionaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
            Text(
                text = "Monthly Budget",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(Dimens.SpacingSm))
            Text(
                text = CurrencyFormatter.formatDisplay(totalIncome, currency),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(Dimens.SpacingMd))

            // Stacked horizontal bar
            val total = (totalFixed + totalSavings + discretionary).coerceAtLeast(1L)
            val fixedFraction = totalFixed.toFloat() / total
            val savingsFraction = totalSavings.toFloat() / total
            val discretionaryFraction = discretionary.toFloat() / total

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.small),
            ) {
                if (fixedFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(fixedFraction)
                            .height(12.dp)
                            .background(fixedColor),
                    )
                }
                if (savingsFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(savingsFraction)
                            .height(12.dp)
                            .background(savingsColor),
                    )
                }
                if (discretionaryFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(discretionaryFraction)
                            .height(12.dp)
                            .background(discretionaryColor),
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingMd))

            // Legend with amounts
            LegendRow(
                color = fixedColor,
                label = "Fixed expenses",
                amount = CurrencyFormatter.formatDisplay(totalFixed, currency),
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            LegendRow(
                color = savingsColor,
                label = "Savings",
                amount = CurrencyFormatter.formatDisplay(totalSavings, currency),
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            LegendRow(
                color = discretionaryColor,
                label = "Discretionary",
                amount = CurrencyFormatter.formatDisplay(discretionary, currency),
            )
        }
    }
}

@Composable
private fun LegendRow(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    amount: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.padding(start = Dimens.SpacingSm))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
