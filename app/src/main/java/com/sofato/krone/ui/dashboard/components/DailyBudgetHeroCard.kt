package com.sofato.krone.ui.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.CategorySpend
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.DailyBudget
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter
import com.sofato.krone.util.IconMapper

@Composable
fun DailyBudgetHeroCard(
    dailyBudget: DailyBudget,
    spentToday: Long,
    currency: Currency,
    modifier: Modifier = Modifier,
    trackedCategories: List<CategorySpend> = emptyList(),
) {
    val remainingToday = (dailyBudget.dailyAmountMinor - spentToday).coerceAtLeast(0)
    val isOverDaily = spentToday > dailyBudget.dailyAmountMinor
    val progress = if (dailyBudget.dailyAmountMinor > 0) {
        (spentToday.toFloat() / dailyBudget.dailyAmountMinor).coerceIn(0f, 1.5f)
    } else 0f

    val progressColor = when {
        progress < 0.7f -> MaterialTheme.colorScheme.primary
        progress < 1.0f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    val containerColor = if (isOverDaily) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isOverDaily) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    // Categories that are over 80% of their budget — worth warning about
    val warningCategories = trackedCategories.filter { cs ->
        cs.allocatedMinor > 0 && cs.spentMinor.toFloat() / cs.allocatedMinor >= 0.80f
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingLg)) {
            Text(
                text = "You can spend",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
            )
            Spacer(Modifier.height(4.dp))
            AnimatedContent(
                targetState = remainingToday,
                label = "daily_budget",
            ) { amount ->
                Text(
                    text = CurrencyFormatter.formatDisplay(amount, currency),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverDaily) {
                        MaterialTheme.colorScheme.error
                    } else {
                        contentColor
                    },
                )
            }
            Text(
                text = "today",
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
            )

            Spacer(Modifier.height(Dimens.SpacingMd))

            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = progressColor,
                trackColor = contentColor.copy(alpha = 0.15f),
            )

            Spacer(Modifier.height(Dimens.SpacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Spent: ${CurrencyFormatter.formatDisplay(spentToday, currency)} / ${CurrencyFormatter.formatDisplay(dailyBudget.dailyAmountMinor, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                )
                Text(
                    text = "${dailyBudget.remainingDays} days left",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                )
            }

            if (isOverDaily) {
                Spacer(Modifier.height(Dimens.SpacingSm))
                Text(
                    text = "You've spent over today's budget. Tomorrow's allowance will adjust.",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                )
            }

            // Category budget warnings
            if (warningCategories.isNotEmpty()) {
                Spacer(Modifier.height(Dimens.SpacingSm))
                HorizontalDivider(color = contentColor.copy(alpha = 0.15f))
                Spacer(Modifier.height(Dimens.SpacingSm))
                warningCategories.forEach { cs ->
                    val fraction = cs.spentMinor.toFloat() / cs.allocatedMinor
                    val isOver = cs.spentMinor > cs.allocatedMinor
                    val remaining = cs.allocatedMinor - cs.spentMinor
                    val categoryColor = try {
                        Color(android.graphics.Color.parseColor(cs.category.colorHex))
                    } catch (_: Exception) {
                        contentColor
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = IconMapper.getIcon(cs.category.iconName),
                            contentDescription = null,
                            tint = if (isOver) MaterialTheme.colorScheme.error else categoryColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(Dimens.SpacingSm))
                        Text(
                            text = cs.category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = if (isOver) {
                                "${CurrencyFormatter.formatDisplay(kotlin.math.abs(remaining), currency)} over"
                            } else {
                                "${CurrencyFormatter.formatDisplay(remaining, currency)} left"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isOver) MaterialTheme.colorScheme.error else contentColor.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}
