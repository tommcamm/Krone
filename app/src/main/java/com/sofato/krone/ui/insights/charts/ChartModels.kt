package com.sofato.krone.ui.insights.charts

import androidx.compose.ui.graphics.Color

data class LineChartData(
    val label: String,
    val value: Long,
)

data class BarChartGroup(
    val label: String,
    val currentValue: Long,
    val previousValue: Long,
    val color: Color,
)

data class StackedBarSegment(
    val label: String,
    val value: Long,
    val color: Color,
)

data class StackedBarData(
    val label: String,
    val segments: List<StackedBarSegment>,
)

data class AreaChartData(
    val label: String,
    val spendingMinor: Long,
    val savingsMinor: Long,
)
