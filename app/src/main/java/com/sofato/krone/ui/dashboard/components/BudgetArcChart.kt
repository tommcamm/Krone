package com.sofato.krone.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.util.CurrencyFormatter

data class ArcSegment(
    val label: String,
    val value: Long,
    val color: Color,
)

@Composable
fun BudgetArcChart(
    totalBudget: Long,
    totalSpent: Long,
    remainingDays: Int,
    currency: Currency,
    segments: List<ArcSegment>,
    modifier: Modifier = Modifier,
) {
    val animationProgress = remember { Animatable(0f) }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(segments) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val remaining = (totalBudget - totalSpent).coerceAtLeast(0)
    val isOverBudget = totalSpent > totalBudget

    val description = segments.joinToString(", ") { seg ->
        val pct = if (totalBudget > 0) seg.value * 100 / totalBudget else 0
        "${seg.label}: $pct%"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = "Budget arc chart: $description" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dim = size.minDimension
            val strokeWidth = dim * 0.065f
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            val segmentStroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            val arcMargin = dim * 0.08f
            val arcPadding = strokeWidth / 2f + arcMargin
            val arcDiameter = dim - arcPadding * 2
            val arcSize = Size(arcDiameter, arcDiameter)
            val topLeft = Offset(
                (size.width - arcDiameter) / 2f,
                (size.height - arcDiameter) / 2f,
            )

            val startAngle = 135f
            val totalSweep = 270f

            // Background track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )

            // Category segments
            if (totalBudget > 0) {
                var currentAngle = startAngle
                val firstSegmentStroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                segments.forEachIndexed { index, segment ->
                    val sweep = (segment.value.toFloat() / totalBudget) * totalSweep * animationProgress.value
                    if (sweep > 0.5f) {
                        drawArc(
                            color = segment.color,
                            startAngle = currentAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = if (index == 0) firstSegmentStroke else segmentStroke,
                        )
                    }
                    currentAngle += sweep
                }
            }
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 48.dp),
        ) {
            Text(
                text = if (isOverBudget) "OVER BUDGET" else "LEFT THIS MONTH",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
                color = if (isOverBudget) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = CurrencyFormatter.formatDisplay(
                    if (isOverBudget) totalSpent - totalBudget else remaining,
                    currency,
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isOverBudget) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
            )
            Text(
                text = "$remainingDays",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Text(
                text = "Days left",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
