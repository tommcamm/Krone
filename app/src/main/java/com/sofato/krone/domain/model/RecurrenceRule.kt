package com.sofato.krone.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus

object RecurrenceRule {
    const val MONTHLY = "MONTHLY"
    const val YEARLY = "YEARLY"

    fun normalize(raw: String?): String {
        return when (raw?.trim()?.uppercase()) {
            YEARLY -> YEARLY
            else -> MONTHLY
        }
    }

    fun advanceNextDate(current: LocalDate, recurrenceRule: String, dayOfMonth: Int? = null): LocalDate {
        val base = when (normalize(recurrenceRule)) {
            YEARLY -> current.plus(1, DateTimeUnit.YEAR)
            else -> current.plus(1, DateTimeUnit.MONTH)
        }
        if (dayOfMonth == null) return base
        return clampToDay(base.year, base.month.number, dayOfMonth)
    }

    /**
     * Computes the initial nextDate for a new recurring expense with a specific charge day.
     * If the day hasn't occurred yet this month, use this month; otherwise, next month.
     */
    fun initialNextDate(dayOfMonth: Int, today: LocalDate): LocalDate {
        val thisMonth = clampToDay(today.year, today.month.number, dayOfMonth)
        return if (thisMonth >= today) {
            thisMonth
        } else {
            val nextMonth = today.plus(1, DateTimeUnit.MONTH)
            clampToDay(nextMonth.year, nextMonth.month.number, dayOfMonth)
        }
    }

    /**
     * Clamps a day-of-month to the last valid day for the given year/month.
     * E.g., day 31 in February becomes Feb 28 (or 29 in a leap year).
     */
    private fun clampToDay(year: Int, month: Int, dayOfMonth: Int): LocalDate {
        val lastDay = lastDayOfMonth(year, month)
        return LocalDate(year, month, dayOfMonth.coerceIn(1, lastDay))
    }

    private fun lastDayOfMonth(year: Int, month: Int): Int {
        // Advance to 1st of next month, subtract 1 day
        val firstOfNext = if (month == 12) {
            LocalDate(year + 1, 1, 1)
        } else {
            LocalDate(year, month + 1, 1)
        }
        return firstOfNext.toEpochDays().minus(LocalDate(year, month, 1).toEpochDays()).toInt()
    }
}
