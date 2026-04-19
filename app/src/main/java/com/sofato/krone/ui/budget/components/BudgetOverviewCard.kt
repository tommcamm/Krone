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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.CategorySpend
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.ui.theme.LocalBudgetBarColors
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun BudgetOverviewCard(
    totalIncome: Long,
    totalFixed: Long,
    totalSavings: Long,
    discretionary: Long,
    currency: Currency,
    modifier: Modifier = Modifier,
    categoryBreakdown: List<CategorySpend> = emptyList(),
    unallocatedDiscretionaryMinor: Long = discretionary,
) {
    val budgetColors = LocalBudgetBarColors.current
    val fixedColor = budgetColors.fixed
    val savingsColor = budgetColors.savings
    val unallocatedColor = budgetColors.discretionary

    // Categories that have a budget allocation
    val allocatedCategories = categoryBreakdown.filter { it.allocatedMinor > 0 }

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

            // Segmented horizontal bar
            val total = (totalFixed + totalSavings + discretionary).coerceAtLeast(1L)

            data class BarSegment(val amount: Long, val color: Color, val label: String)

            val segments = mutableListOf<BarSegment>()
            segments.add(BarSegment(totalFixed, fixedColor, "Fixed expenses"))
            segments.add(BarSegment(totalSavings, savingsColor, "Savings"))

            // Break discretionary into category allocations + unallocated
            if (allocatedCategories.isNotEmpty()) {
                for (cs in allocatedCategories) {
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(cs.category.colorHex))
                    } catch (_: Exception) {
                        unallocatedColor
                    }
                    segments.add(BarSegment(cs.allocatedMinor, catColor, cs.category.name))
                }
                if (unallocatedDiscretionaryMinor > 0) {
                    segments.add(BarSegment(unallocatedDiscretionaryMinor, unallocatedColor, "Free budget"))
                }
            } else {
                segments.add(BarSegment(discretionary, unallocatedColor, "Discretionary"))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.small),
            ) {
                for (segment in segments) {
                    if (segment.amount > 0) {
                        Box(
                            modifier = Modifier
                                .weight(segment.amount.toFloat() / total)
                                .height(12.dp)
                                .background(segment.color),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingMd))

            // Legend
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

            if (allocatedCategories.isNotEmpty()) {
                for (cs in allocatedCategories) {
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(cs.category.colorHex))
                    } catch (_: Exception) {
                        unallocatedColor
                    }
                    LegendRow(
                        color = catColor,
                        label = cs.category.name,
                        amount = CurrencyFormatter.formatDisplay(cs.allocatedMinor, currency),
                    )
                    Spacer(Modifier.height(Dimens.SpacingXs))
                }
                if (unallocatedDiscretionaryMinor > 0) {
                    LegendRow(
                        color = unallocatedColor,
                        label = "Free budget",
                        amount = CurrencyFormatter.formatDisplay(unallocatedDiscretionaryMinor, currency),
                    )
                }
            } else {
                LegendRow(
                    color = unallocatedColor,
                    label = "Discretionary",
                    amount = CurrencyFormatter.formatDisplay(discretionary, currency),
                )
            }
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
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
