package com.sofato.krone.ui.insights.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AreaChartData(
    val label: String,
    val spendingMinor: Long,
    val savingsMinor: Long,
)

@Composable
fun StackedAreaChart(
    data: List<AreaChartData>,
    modifier: Modifier = Modifier,
    spendingColor: Color = MaterialTheme.colorScheme.error,
    savingsColor: Color = MaterialTheme.colorScheme.tertiary,
    chartHeight: Int = 160,
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

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight.dp)
            .semantics { contentDescription = "Savings vs spending over time" },
    ) {
        val bottomPadding = 24.dp.toPx()
        val chartAreaHeight = size.height - bottomPadding
        val maxValue = data.maxOf { it.spendingMinor + it.savingsMinor }.coerceAtLeast(1L)
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        // Grid
        for (i in 0..3) {
            val y = chartAreaHeight * (1f - i / 3f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            )
        }

        // Build spending area path (bottom layer)
        val spendingPath = Path()
        val savingsPath = Path()

        val spendingPoints = data.mapIndexed { i, d ->
            val x = i * stepX
            val y = chartAreaHeight * (1f - (d.spendingMinor.toFloat() / maxValue) * animationProgress.value)
            Offset(x, y)
        }

        val totalPoints = data.mapIndexed { i, d ->
            val x = i * stepX
            val total = d.spendingMinor + d.savingsMinor
            val y = chartAreaHeight * (1f - (total.toFloat() / maxValue) * animationProgress.value)
            Offset(x, y)
        }

        // Spending fill
        spendingPath.moveTo(0f, chartAreaHeight)
        spendingPoints.forEach { spendingPath.lineTo(it.x, it.y) }
        spendingPath.lineTo(spendingPoints.last().x, chartAreaHeight)
        spendingPath.close()

        drawPath(
            path = spendingPath,
            brush = Brush.verticalGradient(
                colors = listOf(spendingColor.copy(alpha = 0.4f), spendingColor.copy(alpha = 0.05f)),
                startY = 0f,
                endY = chartAreaHeight,
            ),
            style = Fill,
        )

        // Savings fill (on top of spending)
        savingsPath.moveTo(spendingPoints.first().x, spendingPoints.first().y)
        totalPoints.forEach { savingsPath.lineTo(it.x, it.y) }
        for (i in spendingPoints.indices.reversed()) {
            savingsPath.lineTo(spendingPoints[i].x, spendingPoints[i].y)
        }
        savingsPath.close()

        drawPath(
            path = savingsPath,
            brush = Brush.verticalGradient(
                colors = listOf(savingsColor.copy(alpha = 0.4f), savingsColor.copy(alpha = 0.05f)),
                startY = 0f,
                endY = chartAreaHeight,
            ),
            style = Fill,
        )

        // Line strokes
        val lineStroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val spendingLinePath = Path()
        spendingLinePath.moveTo(spendingPoints[0].x, spendingPoints[0].y)
        for (i in 1 until spendingPoints.size) {
            spendingLinePath.lineTo(spendingPoints[i].x, spendingPoints[i].y)
        }
        drawPath(spendingLinePath, spendingColor, style = lineStroke)

        val totalLinePath = Path()
        totalLinePath.moveTo(totalPoints[0].x, totalPoints[0].y)
        for (i in 1 until totalPoints.size) {
            totalLinePath.lineTo(totalPoints[i].x, totalPoints[i].y)
        }
        drawPath(totalLinePath, savingsColor, style = lineStroke)

        // X-axis labels
        val labelStep = if (data.size <= 6) 1 else (data.size / 5).coerceAtLeast(1)
        for (i in data.indices step labelStep) {
            val x = i * stepX
            val measured = textMeasurer.measure(data[i].label, TextStyle(fontSize = 10.sp))
            drawText(
                textMeasurer = textMeasurer,
                text = data[i].label,
                topLeft = Offset(x - measured.size.width / 2f, chartAreaHeight + 4.dp.toPx()),
                style = TextStyle(fontSize = 10.sp, color = labelColor),
            )
        }
    }
}
