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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StackedBarSegment(
    val label: String,
    val value: Long,
    val color: Color,
)

data class StackedBarData(
    val label: String,
    val segments: List<StackedBarSegment>,
)

@Composable
fun StackedBarChart(
    data: List<StackedBarData>,
    modifier: Modifier = Modifier,
    chartHeight: Int = 220,
) {
    if (data.isEmpty()) return

    val animationProgress = remember(data) { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val barWidth = 56.dp
    val spacing = 16.dp
    val totalWidth = barWidth * data.size + spacing * (data.size - 1)
    val description = data.joinToString(", ") { bar ->
        "${bar.label}: ${bar.segments.sumOf { it.value }}"
    }

    Box(modifier = modifier.horizontalScroll(rememberScrollState())) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(chartHeight.dp)
                .semantics { contentDescription = "Category trend: $description" },
        ) {
            val maxValue = data.maxOf { bar -> bar.segments.sumOf { it.value } }.coerceAtLeast(1L)
            val bottomPadding = 28.dp.toPx()
            val barAreaHeight = size.height - bottomPadding
            val barWidthPx = barWidth.toPx()
            val spacingPx = spacing.toPx()
            val cornerRadius = CornerRadius(3.dp.toPx())

            data.forEachIndexed { index, bar ->
                val barStart = index * (barWidthPx + spacingPx)
                val barCenter = barStart + barWidthPx / 2f

                // Draw stacked segments bottom-up
                var yOffset = barAreaHeight
                for (segment in bar.segments) {
                    val segmentHeight = (segment.value.toFloat() / maxValue) * barAreaHeight * animationProgress.value
                    if (segmentHeight > 0f) {
                        yOffset -= segmentHeight
                        drawRoundRect(
                            color = segment.color,
                            topLeft = Offset(barStart, yOffset),
                            size = Size(barWidthPx, segmentHeight),
                            cornerRadius = cornerRadius,
                        )
                    }
                }

                // Label
                val labelText = bar.label.takeLast(2).let { m ->
                    val monthNum = m.toIntOrNull() ?: return@let m
                    java.time.Month.of(monthNum).name.take(3).lowercase()
                        .replaceFirstChar { it.uppercase() }
                }
                val measured = textMeasurer.measure(
                    labelText,
                    TextStyle(fontSize = 10.sp),
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    topLeft = Offset(barCenter - measured.size.width / 2f, barAreaHeight + 6.dp.toPx()),
                    style = TextStyle(fontSize = 10.sp, color = labelColor),
                )
            }
        }
    }
}
