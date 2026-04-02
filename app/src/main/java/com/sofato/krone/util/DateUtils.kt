package com.sofato.krone.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

fun LocalDate.Companion.today(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun LocalDate.startOfMonth(): LocalDate = LocalDate(year, month, 1)

fun LocalDate.endOfMonth(): LocalDate {
    val nextMonth = this.startOfMonth().plus(1, DateTimeUnit.MONTH)
    return nextMonth.minus(1, DateTimeUnit.DAY)
}

fun LocalDate.daysRemainingInMonth(): Int {
    val end = endOfMonth()
    return end.dayOfMonth - dayOfMonth + 1
}

fun calculateBudgetPeriod(
    incomeDay: Int,
    referenceDate: LocalDate = LocalDate.today(),
): com.sofato.krone.domain.model.BudgetPeriod {
    val clampedDay = { year: Int, month: kotlinx.datetime.Month ->
        val lastDay = LocalDate(year, month, 1).endOfMonth().dayOfMonth
        incomeDay.coerceAtMost(lastDay)
    }

    val startDate: LocalDate
    val endDate: LocalDate

    if (referenceDate.dayOfMonth >= incomeDay.coerceAtMost(referenceDate.endOfMonth().dayOfMonth)) {
        // Current period started this month
        startDate = LocalDate(referenceDate.year, referenceDate.month, clampedDay(referenceDate.year, referenceDate.month))
        val nextMonth = startDate.plus(1, DateTimeUnit.MONTH)
        val nextClamped = clampedDay(nextMonth.year, nextMonth.month)
        endDate = LocalDate(nextMonth.year, nextMonth.month, nextClamped).minus(1, DateTimeUnit.DAY)
    } else {
        // Current period started last month
        val prevMonth = referenceDate.startOfMonth().minus(1, DateTimeUnit.DAY).startOfMonth()
        startDate = LocalDate(prevMonth.year, prevMonth.month, clampedDay(prevMonth.year, prevMonth.month))
        val thisClamped = clampedDay(referenceDate.year, referenceDate.month)
        endDate = LocalDate(referenceDate.year, referenceDate.month, thisClamped).minus(1, DateTimeUnit.DAY)
    }

    return com.sofato.krone.domain.model.BudgetPeriod(startDate, endDate)
}
