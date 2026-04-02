package com.sofato.krone.ui.budget.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.ui.components.CategoryIcon
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun CategoryProgressBar(
    category: Category,
    spentMinor: Long,
    allocatedMinor: Long,
    currency: Currency,
    modifier: Modifier = Modifier,
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val isOverBudget = allocatedMinor > 0 && spentMinor > allocatedMinor
    val progress = if (allocatedMinor > 0) {
        (spentMinor.toFloat() / allocatedMinor).coerceIn(0f, 1f)
    } else {
        0f
    }
    val progressColor = if (isOverBudget) MaterialTheme.colorScheme.error else categoryColor

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(
            iconName = category.iconName,
            colorHex = category.colorHex,
        )
        Spacer(Modifier.width(Dimens.SpacingSm))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (allocatedMinor > 0) {
                        "${CurrencyFormatter.formatDisplay(spentMinor, currency)} of ${CurrencyFormatter.formatDisplay(allocatedMinor, currency)}"
                    } else {
                        "${CurrencyFormatter.formatDisplay(spentMinor, currency)} spent"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (allocatedMinor > 0) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                )
            } else {
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth(),
                    color = categoryColor,
                    trackColor = categoryColor.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}
