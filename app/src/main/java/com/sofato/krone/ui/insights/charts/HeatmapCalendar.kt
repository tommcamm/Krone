package com.sofato.krone.ui.insights.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sofato.krone.domain.model.DailySpend
import com.sofato.krone.ui.theme.Dimens
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

@Composable
fun HeatmapCalendar(
    dailySpending: List<DailySpend>,
    selectedMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spendByDate = dailySpending.associate { it.date to it.totalMinor }
    val maxSpend = spendByDate.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    val monthName = selectedMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val title = "$monthName ${selectedMonth.year}"

    Column(modifier = modifier) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpacingSm))

        // Day of week headers
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(Dimens.SpacingXs))

        // Calendar grid
        HeatmapGrid(
            year = selectedMonth.year,
            month = selectedMonth.month,
            spendByDate = spendByDate,
            maxSpend = maxSpend,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HeatmapGrid(
    year: Int,
    month: Month,
    spendByDate: Map<LocalDate, Long>,
    maxSpend: Long,
    modifier: Modifier = Modifier,
) {
    val lowColor = MaterialTheme.colorScheme.surfaceVariant
    val highColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surface
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    val firstDay = LocalDate(year, month, 1)
    // Monday = 1, Sunday = 7 (ISO)
    val startOffset = (firstDay.dayOfWeek.ordinal) // Monday=0..Sunday=6
    val daysInMonth = when (month) {
        Month.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    val description = "Spending heatmap for ${month.name.lowercase()} $year"

    Canvas(
        modifier = modifier
            .aspectRatio(7f / rows)
            .semantics { contentDescription = description },
    ) {
        val cellWidth = size.width / 7f
        val cellHeight = size.height / rows
        val cellPadding = 2.dp.toPx()
        val cornerRadius = CornerRadius(4.dp.toPx())

        for (day in 1..daysInMonth) {
            val cellIndex = startOffset + day - 1
            val col = cellIndex % 7
            val row = cellIndex / 7
            val x = col * cellWidth + cellPadding
            val y = row * cellHeight + cellPadding
            val w = cellWidth - cellPadding * 2
            val h = cellHeight - cellPadding * 2

            val date = LocalDate(year, month, day)
            val spent = spendByDate[date]

            val color = if (spent != null && spent > 0) {
                val intensity = (spent.toFloat() / maxSpend).coerceIn(0.15f, 1f)
                lerp(lowColor, highColor, intensity)
            } else {
                emptyColor
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(w, h),
                cornerRadius = cornerRadius,
            )

            // Day number
            val dayText = day.toString()
            val measured = textMeasurer.measure(dayText, TextStyle(fontSize = 9.sp))
            drawText(
                textMeasurer = textMeasurer,
                text = dayText,
                topLeft = Offset(
                    x + (w - measured.size.width) / 2f,
                    y + (h - measured.size.height) / 2f,
                ),
                style = TextStyle(fontSize = 9.sp, color = textColor),
            )
        }
    }
}
