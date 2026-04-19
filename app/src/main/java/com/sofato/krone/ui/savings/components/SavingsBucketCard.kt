package com.sofato.krone.ui.savings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun SavingsBucketCard(
    bucket: SavingsBucket,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val targetAmount = bucket.targetAmountMinor?.takeIf { it > 0 }
    val hasTarget = targetAmount != null
    val progress = if (targetAmount != null) {
        (bucket.currentBalanceMinor.toFloat() / targetAmount).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasTarget) {
                SavingsProgressArc(
                    progress = progress,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(Dimens.SpacingSm))
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            ) {
                Text(
                    text = bucket.label,
                    style = MaterialTheme.typography.titleSmall,
                )
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = bucket.type.displayName,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${CurrencyFormatter.formatDisplay(bucket.monthlyContributionMinor, currency)}/month",
                    style = MaterialTheme.typography.titleSmall,
                )
                if (targetAmount != null) {
                    Text(
                        text = "of ${CurrencyFormatter.formatDisplay(targetAmount, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
