package com.sofato.krone.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

data class BudgetPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    val totalDays: Int get() = startDate.daysUntil(endDate) + 1

    fun remainingDaysFrom(today: LocalDate): Int {
        val remaining = today.daysUntil(endDate) + 1
        return remaining.coerceAtLeast(1)
    }
}
