package com.sofato.krone.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
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

    fun advanceNextDate(current: LocalDate, recurrenceRule: String): LocalDate {
        return when (normalize(recurrenceRule)) {
            YEARLY -> current.plus(1, DateTimeUnit.YEAR)
            else -> current.plus(1, DateTimeUnit.MONTH)
        }
    }
}
