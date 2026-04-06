package com.sofato.krone.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class ArcSegment(
    val label: String,
    val value: Long,
    val color: Color,
)

@OptIn(ExperimentalLayoutApi::class)
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
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

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

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
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
                val arcMargin = dim * 0.12f
                val arcPadding = strokeWidth / 2f + arcMargin
                val arcDiameter = dim - arcPadding * 2
                val arcSize = Size(arcDiameter, arcDiameter)
                val arcCenter = Offset(size.width / 2f, size.height / 2f)
                val arcRadius = arcDiameter / 2f
                val topLeft = Offset(
                    (size.width - arcDiameter) / 2f,
                    (size.height - arcDiameter) / 2f,
                )

                val startAngle = 240f
                val totalSweep = 240f

                // Budget limit outline (thin red ring behind everything)
                val limitStroke = Stroke(width = strokeWidth * 1.3f, cap = StrokeCap.Round)
                drawArc(
                    color = Color(0xFFEF4444).copy(alpha = 0.18f),
                    startAngle = startAngle,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = limitStroke,
                )

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

                // Budget endpoint tick marks
                if (animationProgress.value > 0.9f) {
                    val tickLength = dim * 0.04f
                    val tickLabelStyle = TextStyle(
                        fontSize = 9.sp,
                        color = labelColor,
                    )

                    // Start tick (0) at startAngle (240° = 8 o'clock, left side)
                    val startRad = (startAngle * PI / 180.0).toFloat()
                    val startInner = Offset(
                        arcCenter.x + (arcRadius + strokeWidth / 2f) * cos(startRad),
                        arcCenter.y + (arcRadius + strokeWidth / 2f) * sin(startRad),
                    )
                    val startOuter = Offset(
                        arcCenter.x + (arcRadius + strokeWidth / 2f + tickLength) * cos(startRad),
                        arcCenter.y + (arcRadius + strokeWidth / 2f + tickLength) * sin(startRad),
                    )
                    drawLine(color = labelColor, start = startInner, end = startOuter, strokeWidth = 2f)
                    val zeroResult = textMeasurer.measure(
                        CurrencyFormatter.formatDisplay(0, currency),
                        tickLabelStyle,
                    )
                    // Label to the left of the tick (gap side)
                    drawText(
                        zeroResult,
                        topLeft = Offset(
                            startOuter.x - zeroResult.size.width - 8f,
                            startOuter.y - zeroResult.size.height / 2f,
                        ),
                    )

                    // End tick (budget) at startAngle+sweep (480°=120° = 4 o'clock, left side)
                    val endRad = ((startAngle + totalSweep) * PI / 180.0).toFloat()
                    val endInner = Offset(
                        arcCenter.x + (arcRadius + strokeWidth / 2f) * cos(endRad),
                        arcCenter.y + (arcRadius + strokeWidth / 2f) * sin(endRad),
                    )
                    val endOuter = Offset(
                        arcCenter.x + (arcRadius + strokeWidth / 2f + tickLength) * cos(endRad),
                        arcCenter.y + (arcRadius + strokeWidth / 2f + tickLength) * sin(endRad),
                    )
                    drawLine(color = labelColor, start = endInner, end = endOuter, strokeWidth = 2f)
                    val budgetText = CurrencyFormatter.formatDisplay(totalBudget, currency)
                    val budgetResult = textMeasurer.measure(budgetText, tickLabelStyle)
                    // Label to the left of the tick (gap side)
                    drawText(
                        budgetResult,
                        topLeft = Offset(
                            endOuter.x - budgetResult.size.width - 8f,
                            endOuter.y - budgetResult.size.height / 2f,
                        ),
                    )
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

        // Legend row
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            segments.forEach { segment ->
                val pct = if (totalBudget > 0) segment.value * 100 / totalBudget else 0
                val name = if (segment.label.length > 6) {
                    segment.label.take(5).trimEnd() + "."
                } else {
                    segment.label
                }
                LegendItem(
                    color = segment.color,
                    text = "$name $pct%",
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = Dimens.SpacingSm),
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(Modifier.width(Dimens.SpacingXs))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}
