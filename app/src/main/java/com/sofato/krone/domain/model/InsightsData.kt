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

enum class InsightType { POSITIVE, NEGATIVE, NEUTRAL }

data class SpendingStreak(
    val currentDays: Int,
    val longestDays: Int,
)
