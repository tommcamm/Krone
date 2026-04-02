package com.sofato.krone.ui.insights.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarChartGroup(
    val label: String,
    val currentValue: Long,
    val previousValue: Long,
    val color: Color,
)

@Composable
fun GroupedBarChart(
    groups: List<BarChartGroup>,
    modifier: Modifier = Modifier,
    chartHeight: Int = 200,
) {
    if (groups.isEmpty()) return

    val animationProgress = remember(groups) { Animatable(0f) }
    LaunchedEffect(groups) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val groupWidth = 72.dp
    val totalWidth = groupWidth * groups.size
    val description = groups.joinToString(", ") { "${it.label}: ${it.currentValue} vs ${it.previousValue}" }

    Box(modifier = modifier.horizontalScroll(rememberScrollState())) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(chartHeight.dp)
                .semantics { contentDescription = "Category comparison: $description" },
        ) {
            val maxValue = groups.maxOf { maxOf(it.currentValue, it.previousValue) }.coerceAtLeast(1L)
            val bottomPadding = 28.dp.toPx()
            val barAreaHeight = size.height - bottomPadding
            val groupWidthPx = groupWidth.toPx()
            val barWidth = groupWidthPx * 0.3f
            val gap = groupWidthPx * 0.05f
            val cornerRadius = CornerRadius(3.dp.toPx())

            groups.forEachIndexed { index, group ->
                val groupStart = index * groupWidthPx
                val centerX = groupStart + groupWidthPx / 2f

                // Previous bar (lighter)
                val prevHeight = (group.previousValue.toFloat() / maxValue) * barAreaHeight * animationProgress.value
                if (prevHeight > 0f) {
                    drawRoundRect(
                        color = group.color.copy(alpha = 0.35f),
                        topLeft = Offset(centerX - barWidth - gap / 2, barAreaHeight - prevHeight),
                        size = Size(barWidth, prevHeight),
                        cornerRadius = cornerRadius,
                    )
                }

                // Current bar
                val currHeight = (group.currentValue.toFloat() / maxValue) * barAreaHeight * animationProgress.value
                if (currHeight > 0f) {
                    drawRoundRect(
                        color = group.color,
                        topLeft = Offset(centerX + gap / 2, barAreaHeight - currHeight),
                        size = Size(barWidth, currHeight),
                        cornerRadius = cornerRadius,
                    )
                }

                // Label
                val labelText = if (group.label.length > 8) group.label.take(7) + "\u2026" else group.label
                val measured = textMeasurer.measure(
                    labelText,
                    TextStyle(fontSize = 10.sp),
                    overflow = TextOverflow.Clip,
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    topLeft = Offset(centerX - measured.size.width / 2f, barAreaHeight + 6.dp.toPx()),
                    style = TextStyle(fontSize = 10.sp, color = labelColor),
                )
            }
        }
    }
}
