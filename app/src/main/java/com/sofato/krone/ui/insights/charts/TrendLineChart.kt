package com.sofato.krone.ui.insights.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LineChartData(
    val label: String,
    val value: Long,
)

@Composable
fun TrendLineChart(
    data: List<LineChartData>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    idealLine: List<LineChartData>? = null,
    idealLineColor: Color = MaterialTheme.colorScheme.outline,
    chartHeight: Int = 180,
) {
    if (data.size < 2) return

    val animationProgress = remember(data) { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val description = data.joinToString(", ") { "${it.label}: ${it.value}" }

    Column(modifier = modifier.semantics { contentDescription = "Trend: $description" }) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight.dp)
                .padding(start = 4.dp, end = 4.dp),
        ) {
            val allValues = data.map { it.value } + (idealLine?.map { it.value } ?: emptyList())
            val maxValue = allValues.max().coerceAtLeast(1L)
            val minValue = 0L

            val leftPadding = 0f
            val bottomPadding = 24.dp.toPx()
            val chartWidth = size.width - leftPadding
            val chartAreaHeight = size.height - bottomPadding

            // Grid lines
            for (i in 0..3) {
                val y = chartAreaHeight * (1f - i / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
                )
            }

            // Ideal line (dashed)
            if (idealLine != null && idealLine.size >= 2) {
                drawDataLine(
                    data = idealLine,
                    maxValue = maxValue,
                    leftPadding = leftPadding,
                    chartWidth = chartWidth,
                    chartHeight = chartAreaHeight,
                    color = idealLineColor,
                    progress = 1f,
                    dashed = true,
                )
            }

            // Main line
            drawDataLine(
                data = data,
                maxValue = maxValue,
                leftPadding = leftPadding,
                chartWidth = chartWidth,
                chartHeight = chartAreaHeight,
                color = lineColor,
                progress = animationProgress.value,
                dashed = false,
            )

            // X-axis labels
            val labelStep = if (data.size <= 6) 1 else (data.size / 5).coerceAtLeast(1)
            for (i in data.indices step labelStep) {
                val x = leftPadding + (i.toFloat() / (data.size - 1)) * chartWidth
                drawText(
                    textMeasurer = textMeasurer,
                    text = data[i].label,
                    topLeft = Offset(
                        x - textMeasurer.measure(
                            data[i].label,
                            TextStyle(fontSize = 10.sp),
                        ).size.width / 2f,
                        chartAreaHeight + 4.dp.toPx(),
                    ),
                    style = TextStyle(fontSize = 10.sp, color = labelColor),
                )
            }
        }
    }
}

private fun DrawScope.drawDataLine(
    data: List<LineChartData>,
    maxValue: Long,
    leftPadding: Float,
    chartWidth: Float,
    chartHeight: Float,
    color: Color,
    progress: Float,
    dashed: Boolean,
) {
    if (data.size < 2) return
    val points = data.mapIndexed { index, item ->
        val x = leftPadding + (index.toFloat() / (data.size - 1)) * chartWidth
        val y = chartHeight * (1f - item.value.toFloat() / maxValue.coerceAtLeast(1L))
        Offset(x, y)
    }

    val path = Path()
    path.moveTo(points[0].x, points[0].y)

    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        val cpx = (prev.x + curr.x) / 2f
        path.cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
    }

    val stroke = Stroke(
        width = 2.5.dp.toPx(),
        cap = StrokeCap.Round,
        pathEffect = if (dashed) PathEffect.dashPathEffect(
            floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
        ) else null,
    )

    // Animate by clipping progress
    drawContext.canvas.save()
    drawContext.canvas.clipRect(
        0f,
        0f,
        leftPadding + chartWidth * progress,
        size.height,
    )
    drawPath(path = path, color = color, style = stroke)
    drawContext.canvas.restore()

    // Draw dots
    if (!dashed) {
        val visibleCount = (points.size * progress).toInt()
        for (i in 0 until visibleCount.coerceAtMost(points.size)) {
            drawCircle(
                color = color,
                radius = 3.5.dp.toPx(),
                center = points[i],
            )
        }
    }
}
