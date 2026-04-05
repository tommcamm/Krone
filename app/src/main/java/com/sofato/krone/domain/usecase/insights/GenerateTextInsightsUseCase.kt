package com.sofato.krone.domain.usecase.insights

import com.sofato.krone.domain.model.CategoryMonthlySpend
import com.sofato.krone.domain.model.InsightData
import com.sofato.krone.domain.model.MonthlySnapshot
import com.sofato.krone.domain.model.SpendingStreak
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

class GenerateTextInsightsUseCase @Inject constructor() {
    operator fun invoke(
        categoryComparison: List<CategoryMonthlySpend>,
        snapshots: List<MonthlySnapshot>,
        streak: SpendingStreak,
    ): List<InsightData> {
        val insights = mutableListOf<InsightData>()

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
                    insights += InsightData.CategoryChangeUp(pctChange, biggestChange.category.name)
                } else {
                    insights += InsightData.CategoryChangeDown(abs(pctChange), biggestChange.category.name)
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
                        insights += InsightData.OverallSpendingUp(pct)
                    } else {
                        insights += InsightData.OverallSpendingDown(abs(pct))
                    }
                }
            }
        }

        // 3. Streak callout
        if (streak.currentDays >= 3) {
            insights += InsightData.StreakCallout(streak.currentDays)
        }

        // 4. Top category (if we still have room)
        if (insights.size < 3 && categoryComparison.isNotEmpty()) {
            val top = categoryComparison.first()
            if (top.currentMonthMinor > 0) {
                insights += InsightData.TopCategory(top.category.name)
            }
        }

        return insights.take(3)
    }
}
