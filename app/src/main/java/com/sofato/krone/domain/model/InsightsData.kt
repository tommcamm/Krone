package com.sofato.krone.domain.model

import kotlinx.datetime.LocalDate

data class DailySpend(
    val date: LocalDate,
    val totalMinor: Long,
)

data class CategoryMonthlySpend(
    val category: Category,
    val currentMonthMinor: Long,
    val previousMonthMinor: Long,
)

data class CurrencyBreakdownItem(
    val currencyCode: String,
    val symbol: String,
    val originalTotalMinor: Long,
    val homeTotalMinor: Long,
    val decimalPlaces: Int,
)

data class TextInsight(
    val message: String,
    val type: InsightType,
)

/** Structured insight returned by the domain layer (no string resolution). */
sealed class InsightData(val type: InsightType) {
    data class CategoryChangeUp(val percent: Int, val categoryName: String) : InsightData(InsightType.NEGATIVE)
    data class CategoryChangeDown(val percent: Int, val categoryName: String) : InsightData(InsightType.POSITIVE)
    data class OverallSpendingUp(val percent: Int) : InsightData(InsightType.NEGATIVE)
    data class OverallSpendingDown(val percent: Int) : InsightData(InsightType.POSITIVE)
    data class StreakCallout(val days: Int) : InsightData(InsightType.POSITIVE)
    data class TopCategory(val categoryName: String) : InsightData(InsightType.NEUTRAL)
}

enum class InsightType { POSITIVE, NEGATIVE, NEUTRAL }

data class SpendingStreak(
    val currentDays: Int,
    val longestDays: Int,
)

// Expense aggregation projections (used by repository queries)
data class DailyTotal(
    val date: LocalDate,
    val totalMinor: Long,
)

data class CategoryTotal(
    val categoryId: Long,
    val totalMinor: Long,
)

data class CurrencyTotal(
    val currencyCode: String,
    val originalTotalMinor: Long,
    val homeTotalMinor: Long,
)
