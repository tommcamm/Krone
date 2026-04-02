package com.sofato.krone.domain.usecase.insights

import android.content.Context
import com.sofato.krone.R
import com.sofato.krone.domain.model.CategoryMonthlySpend
import com.sofato.krone.domain.model.InsightType
import com.sofato.krone.domain.model.MonthlySnapshot
import com.sofato.krone.domain.model.SpendingStreak
import com.sofato.krone.domain.model.TextInsight
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

class GenerateTextInsightsUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    operator fun invoke(
        categoryComparison: List<CategoryMonthlySpend>,
        snapshots: List<MonthlySnapshot>,
        streak: SpendingStreak,
    ): List<TextInsight> {
        val insights = mutableListOf<TextInsight>()

        // 1. Biggest category change vs last month
        val biggestChange = categoryComparison
            .filter { it.previousMonthMinor > 0 }
            .maxByOrNull {
                abs(it.currentMonthMinor - it.previousMonthMinor).toDouble() / it.previousMonthMinor
            }
        if (biggestChange != null && biggestChange.previousMonthMinor > 0) {
            val pctChange = ((biggestChange.currentMonthMinor - biggestChange.previousMonthMinor).toDouble() /
                biggestChange.previousMonthMinor * 100).roundToInt()
            if (abs(pctChange) >= 10) {
                if (pctChange > 0) {
                    insights += TextInsight(
                        message = context.getString(
                            R.string.insights_spent_more_format,
                            pctChange,
                            biggestChange.category.name,
                        ),
                        type = InsightType.NEGATIVE,
                    )
                } else {
                    insights += TextInsight(
                        message = context.getString(
                            R.string.insights_spent_less_format,
                            abs(pctChange),
                            biggestChange.category.name,
                        ),
                        type = InsightType.POSITIVE,
                    )
                }
            }
        }

        // 2. Overall spending trend (latest two snapshots)
        if (snapshots.size >= 2) {
            val latest = snapshots[0]
            val previous = snapshots[1]
            if (previous.totalVariableMinor > 0) {
                val pct = ((latest.totalVariableMinor - previous.totalVariableMinor).toDouble() /
                    previous.totalVariableMinor * 100).roundToInt()
                if (abs(pct) >= 5) {
                    if (pct > 0) {
                        insights += TextInsight(
                            message = context.getString(R.string.insights_overall_up_format, pct),
                            type = InsightType.NEGATIVE,
                        )
                    } else {
                        insights += TextInsight(
                            message = context.getString(R.string.insights_overall_down_format, abs(pct)),
                            type = InsightType.POSITIVE,
                        )
                    }
                }
            }
        }

        // 3. Streak callout
        if (streak.currentDays >= 3) {
            insights += TextInsight(
                message = context.getString(R.string.insights_streak_callout_format, streak.currentDays),
                type = InsightType.POSITIVE,
            )
        }

        // 4. Top category (if we still have room)
        if (insights.size < 3 && categoryComparison.isNotEmpty()) {
            val top = categoryComparison.first()
            if (top.currentMonthMinor > 0) {
                insights += TextInsight(
                    message = context.getString(R.string.insights_top_category_format, top.category.name),
                    type = InsightType.NEUTRAL,
                )
            }
        }

        return insights.take(3)
    }
}
