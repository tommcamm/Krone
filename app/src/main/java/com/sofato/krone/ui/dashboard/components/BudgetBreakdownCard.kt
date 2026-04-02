package com.sofato.krone.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.DailyBudget
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun BudgetBreakdownCard(
    dailyBudget: DailyBudget,
    spentToday: Long,
    currency: Currency,
    onManageCommitments: () -> Unit,
    onManageSalary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val incomeMinor = dailyBudget.totalIncomeMinor
    val fixedMinor = dailyBudget.totalFixedMinor
    val savingsMinor = dailyBudget.totalSavingsMinor
    val discretionaryMinor = dailyBudget.discretionaryMinor
    val totalSpent = dailyBudget.spentSoFarMinor + spentToday
    val monthlyRemaining = discretionaryMinor - totalSpent
    val isOverBudget = monthlyRemaining < 0
    val spentFraction = if (discretionaryMinor > 0) {
        totalSpent.toFloat() / discretionaryMinor
    } else 0f
    val progressColor = when {
        spentFraction < 0.7f -> MaterialTheme.colorScheme.primary
        spentFraction < 1.0f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOverBudget) "Over budget" else "Left this month",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val displayAmount = kotlin.math.abs(monthlyRemaining)
                    val prefix = if (isOverBudget) "−\u2009" else ""
                    Text(
                        text = prefix + CurrencyFormatter.formatDisplay(displayAmount, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error
                        else androidx.compose.ui.graphics.Color.Unspecified,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Dimens.SpacingSm))

            LinearProgressIndicator(
                progress = { spentFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.small),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            )
            Spacer(Modifier.height(Dimens.SpacingXs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Spent: ${CurrencyFormatter.formatDisplay(totalSpent, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor,
                )
                Text(
                    text = "of ${CurrencyFormatter.formatDisplay(discretionaryMinor, currency)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    HorizontalDivider()
                    Spacer(Modifier.height(Dimens.SpacingSm))

                    BreakdownRow(
                        label = "Income",
                        amount = incomeMinor,
                        currency = currency,
                    )
                    BreakdownRow(
                        label = "Fixed commitments",
                        amount = -fixedMinor,
                        currency = currency,
                        color = MaterialTheme.colorScheme.error,
                    )
                    BreakdownRow(
                        label = "Savings",
                        amount = -savingsMinor,
                        currency = currency,
                        color = MaterialTheme.colorScheme.tertiary,
                    )

                    Spacer(Modifier.height(Dimens.SpacingXs))
                    HorizontalDivider()
                    Spacer(Modifier.height(Dimens.SpacingXs))

                    BreakdownRow(
                        label = "Discretionary",
                        amount = discretionaryMinor,
                        currency = currency,
                        fontWeight = FontWeight.SemiBold,
                    )
                    BreakdownRow(
                        label = "Spent so far",
                        amount = -totalSpent,
                        currency = currency,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BreakdownRow(
                        label = "Remaining",
                        amount = monthlyRemaining,
                        currency = currency,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error
                        else androidx.compose.ui.graphics.Color.Unspecified,
                    )

                    Spacer(Modifier.height(Dimens.SpacingSm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onManageSalary) {
                            Text("Edit income")
                        }
                        TextButton(onClick = onManageCommitments) {
                            Text("Edit fixed costs")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    amount: Long,
    currency: Currency,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingXs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
        )
        val prefix = if (amount < 0) "−\u2009" else ""
        Text(
            text = prefix + CurrencyFormatter.formatDisplay(
                kotlin.math.abs(amount),
                currency,
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = color,
        )
    }
}
