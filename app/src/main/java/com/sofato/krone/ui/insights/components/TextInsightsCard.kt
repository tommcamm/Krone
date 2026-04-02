package com.sofato.krone.ui.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sofato.krone.R
import com.sofato.krone.domain.model.InsightType
import com.sofato.krone.domain.model.TextInsight
import com.sofato.krone.ui.theme.Dimens

@Composable
fun TextInsightsCard(
    insights: List<TextInsight>,
    modifier: Modifier = Modifier,
) {
    if (insights.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        ) {
            Text(
                text = stringResource(R.string.insights_text_insights_title),
                style = MaterialTheme.typography.titleMedium,
            )

            insights.forEach { insight ->
                InsightRow(insight)
            }
        }
    }
}

@Composable
private fun InsightRow(insight: TextInsight) {
    val (icon, tint) = when (insight.type) {
        InsightType.POSITIVE -> Icons.AutoMirrored.Filled.TrendingDown to MaterialTheme.colorScheme.tertiary
        InsightType.NEGATIVE -> Icons.AutoMirrored.Filled.TrendingUp to MaterialTheme.colorScheme.error
        InsightType.NEUTRAL -> Icons.AutoMirrored.Filled.TrendingFlat to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Dimens.IconSizeMedium),
        )
        Spacer(Modifier.width(Dimens.SpacingSm))
        Text(
            text = insight.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
