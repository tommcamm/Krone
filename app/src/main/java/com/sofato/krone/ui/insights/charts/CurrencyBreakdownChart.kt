package com.sofato.krone.ui.insights.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.CurrencyBreakdownItem
import com.sofato.krone.ui.theme.Dimens

@Composable
fun CurrencyBreakdownChart(
    items: List<CurrencyBreakdownItem>,
    formatAmount: (Long, Int) -> String,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val maxHome = items.maxOf { it.homeTotalMinor }.coerceAtLeast(1L)
    val animationProgress = remember(items) { Animatable(0f) }
    LaunchedEffect(items) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val description = items.joinToString(", ") { "${it.currencyCode}: ${it.originalTotalMinor}" }

    Column(
        modifier = modifier.semantics { contentDescription = "Currency breakdown: $description" },
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${item.symbol} ${item.currencyCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(64.dp),
                )

                Spacer(Modifier.width(Dimens.SpacingSm))

                Box(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = {
                            (item.homeTotalMinor.toFloat() / maxHome * animationProgress.value)
                                .coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = barColor,
                        trackColor = trackColor,
                        strokeCap = StrokeCap.Round,
                    )
                }

                Spacer(Modifier.width(Dimens.SpacingSm))

                Text(
                    text = formatAmount(item.originalTotalMinor, item.decimalPlaces),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
