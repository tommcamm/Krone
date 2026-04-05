package com.sofato.krone.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.R
import com.sofato.krone.domain.model.InsightData
import com.sofato.krone.domain.model.TextInsight
import com.sofato.krone.ui.insights.charts.AreaChartData
import com.sofato.krone.ui.insights.charts.BarChartGroup
import com.sofato.krone.ui.insights.charts.CurrencyBreakdownChart
import com.sofato.krone.ui.insights.charts.DonutChart
import com.sofato.krone.ui.insights.charts.DonutSlice
import com.sofato.krone.ui.insights.charts.GroupedBarChart
import com.sofato.krone.ui.insights.charts.HeatmapCalendar
import com.sofato.krone.ui.insights.charts.LineChartData
import com.sofato.krone.ui.insights.charts.StackedAreaChart
import com.sofato.krone.ui.insights.charts.StackedBarChart
import com.sofato.krone.ui.insights.charts.StackedBarData
import com.sofato.krone.ui.insights.charts.StackedBarSegment
import com.sofato.krone.ui.insights.charts.TrendLineChart
import com.sofato.krone.ui.insights.components.ChartCard
import com.sofato.krone.ui.insights.components.StreakCard
import com.sofato.krone.ui.insights.components.TextInsightsCard
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val homeCurrency by viewModel.homeCurrency.collectAsState()
    val dailyBudget by viewModel.dailyBudget.collectAsState()
    val budgetOverview by viewModel.budgetOverview.collectAsState()
    val dailySpending by viewModel.dailySpending.collectAsState()
    val heatmapDailySpending by viewModel.heatmapDailySpending.collectAsState()
    val selectedHeatmapMonth by viewModel.selectedHeatmapMonth.collectAsState()
    val categoryComparison by viewModel.categoryComparison.collectAsState()
    val currencyBreakdown by viewModel.currencyBreakdown.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val insightData by viewModel.insightData.collectAsState()
    val spendingTrend by viewModel.spendingTrend.collectAsState()
    val categoryTrend by viewModel.categoryTrend.collectAsState()

    val currency = homeCurrency ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
    ) {
        item { Spacer(Modifier.height(Dimens.SpacingSm)) }

        // 1. Text insights
        if (insightData.isNotEmpty()) {
            item {
                val textInsights = insightData.map { it.toTextInsight() }
                TextInsightsCard(insights = textInsights)
            }
        }

        // 2. Streak
        item {
            StreakCard(streak = streak)
        }

        // 3. Heatmap
        item {
            ChartCard(title = stringResource(R.string.insights_heatmap_title)) {
                HeatmapCalendar(
                    dailySpending = heatmapDailySpending,
                    selectedMonth = selectedHeatmapMonth,
                    onPreviousMonth = viewModel::previousHeatmapMonth,
                    onNextMonth = viewModel::nextHeatmapMonth,
                )
            }
        }

        // 4. Donut chart
        val overview = budgetOverview
        if (overview != null && overview.categoryBreakdown.isNotEmpty()) {
            item {
                ChartCard(title = stringResource(R.string.insights_donut_title)) {
                    val remaining = (overview.discretionaryMinor - overview.spentMinor)
                        .coerceAtLeast(0L)
                    val categorySlices = overview.categoryBreakdown.map { cs ->
                        DonutSlice(
                            label = cs.category.name,
                            value = cs.spentMinor,
                            color = parseColor(cs.category.colorHex),
                        )
                    }
                    val remainingColor = MaterialTheme.colorScheme.surfaceVariant
                    val slices = if (remaining > 0) {
                        categorySlices + DonutSlice(
                            label = stringResource(R.string.insights_remaining),
                            value = remaining,
                            color = remainingColor,
                        )
                    } else {
                        categorySlices
                    }
                    val centerText = CurrencyFormatter.formatDisplay(remaining, currency)

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        DonutChart(
                            slices = slices,
                            centerText = centerText,
                        )
                    }

                    Spacer(Modifier.height(Dimens.SpacingSm))

                    // Legend (categories only)
                    overview.categoryBreakdown.take(6).forEach { cs ->
                        DonutLegendRow(
                            color = parseColor(cs.category.colorHex),
                            label = cs.category.name,
                            amount = CurrencyFormatter.formatDisplay(cs.spentMinor, currency),
                        )
                    }
                    if (remaining > 0) {
                        DonutLegendRow(
                            color = remainingColor,
                            label = stringResource(R.string.insights_remaining),
                            amount = CurrencyFormatter.formatDisplay(remaining, currency),
                        )
                    }
                }
            }
        }

        // 5. Daily cumulative
        if (dailySpending.size >= 2 && dailyBudget != null) {
            item {
                val budget = dailyBudget!!
                ChartCard(
                    title = stringResource(R.string.insights_cumulative_title),
                    subtitle = stringResource(R.string.insights_cumulative_subtitle),
                ) {
                    // Cumulative actual spend
                    var cumulative = 0L
                    val cumulativeData = dailySpending.map { ds ->
                        cumulative += ds.totalMinor
                        LineChartData(
                            label = ds.date.day.toString(),
                            value = cumulative,
                        )
                    }

                    // Ideal line: linear from 0 to discretionary budget
                    val idealData = dailySpending.mapIndexed { index, ds ->
                        val idealAmount = budget.discretionaryMinor *
                            (index + 1) / budget.remainingDays.coerceAtLeast(1).toLong()
                        LineChartData(
                            label = ds.date.day.toString(),
                            value = idealAmount.coerceAtLeast(0L),
                        )
                    }

                    TrendLineChart(
                        data = cumulativeData,
                        idealLine = idealData,
                    )
                }
            }
        }

        // 6. Category comparison
        if (categoryComparison.isNotEmpty()) {
            item {
                ChartCard(
                    title = stringResource(R.string.insights_comparison_title),
                    subtitle = stringResource(R.string.insights_comparison_subtitle),
                ) {
                    val groups = categoryComparison.take(8).map { cs ->
                        BarChartGroup(
                            label = cs.category.name,
                            currentValue = cs.currentMonthMinor,
                            previousValue = cs.previousMonthMinor,
                            color = parseColor(cs.category.colorHex),
                        )
                    }
                    GroupedBarChart(groups = groups)
                }
            }
        }

        // 7. Category trend (stacked bar chart)
        if (categoryTrend.size >= 2 && categoryTrend.any { it.categories.isNotEmpty() }) {
            item {
                ChartCard(title = stringResource(R.string.insights_category_trend_title)) {
                    val barData = categoryTrend.map { month ->
                        StackedBarData(
                            label = month.month,
                            segments = month.categories.take(6).map { ca ->
                                StackedBarSegment(
                                    label = ca.categoryName,
                                    value = ca.amountMinor,
                                    color = parseColor(ca.colorHex),
                                )
                            },
                        )
                    }
                    StackedBarChart(data = barData)
                }
            }
        }

        // 8. Spending trend line
        if (spendingTrend.size >= 2) {
            item {
                ChartCard(title = stringResource(R.string.insights_trend_title)) {
                    val trendData = spendingTrend.map { trend ->
                        val monthLabel = trend.month.takeLast(2).let { m ->
                            val monthNum = m.toIntOrNull() ?: return@let m
                            java.time.Month.of(monthNum).name.take(3).lowercase()
                                .replaceFirstChar { it.uppercase() }
                        }
                        LineChartData(label = monthLabel, value = trend.totalSpendingMinor)
                    }
                    TrendLineChart(data = trendData)
                }
            }
        }

        // 8. Savings vs spending area chart
        if (spendingTrend.size >= 2 && spendingTrend.any { it.totalSavingsMinor > 0 }) {
            item {
                ChartCard(title = stringResource(R.string.insights_savings_ratio_title)) {
                    val areaData = spendingTrend.map { trend ->
                        val monthLabel = trend.month.takeLast(2).let { m ->
                            val monthNum = m.toIntOrNull() ?: return@let m
                            java.time.Month.of(monthNum).name.take(3).lowercase()
                                .replaceFirstChar { it.uppercase() }
                        }
                        AreaChartData(
                            label = monthLabel,
                            spendingMinor = trend.totalSpendingMinor,
                            savingsMinor = trend.totalSavingsMinor,
                        )
                    }
                    StackedAreaChart(data = areaData)
                }
            }
        }

        // 9. Currency breakdown
        if (currencyBreakdown.isNotEmpty()) {
            item {
                ChartCard(title = stringResource(R.string.insights_currency_breakdown_title)) {
                    CurrencyBreakdownChart(
                        items = currencyBreakdown,
                        formatAmount = { amount, decimals ->
                            CurrencyFormatter.formatPlain(amount, decimals)
                        },
                    )
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(Dimens.FabSpacerHeight)) }
    }
}

@Composable
private fun DonutLegendRow(
    color: Color,
    label: String,
    amount: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingXxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(Dimens.IconSizeSmall)) {
                drawCircle(color = color)
            }
            Spacer(Modifier.padding(start = Dimens.SpacingSm))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun InsightData.toTextInsight(): TextInsight {
    val message = when (this) {
        is InsightData.CategoryChangeUp -> stringResource(R.string.insights_spent_more_format, percent, categoryName)
        is InsightData.CategoryChangeDown -> stringResource(R.string.insights_spent_less_format, percent, categoryName)
        is InsightData.OverallSpendingUp -> stringResource(R.string.insights_overall_up_format, percent)
        is InsightData.OverallSpendingDown -> stringResource(R.string.insights_overall_down_format, percent)
        is InsightData.StreakCallout -> stringResource(R.string.insights_streak_callout_format, days)
        is InsightData.TopCategory -> stringResource(R.string.insights_top_category_format, categoryName)
    }
    return TextInsight(message = message, type = type)
}

private fun parseColor(hex: String): Color {
    return try {
        Color((if (hex.startsWith("#")) hex else "#$hex").toColorInt())
    } catch (_: Exception) {
        Color.Gray
    }
}
