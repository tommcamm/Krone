package com.sofato.krone.ui.expenses

import com.sofato.krone.util.endOfMonth
import com.sofato.krone.util.startOfMonth
import com.sofato.krone.util.today
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

sealed interface DateRange {
    data object AllTime : DateRange
    data object ThisMonth : DateRange
    data object LastMonth : DateRange
    data object Last3Months : DateRange
    data object ThisYear : DateRange
    data class Custom(val start: LocalDate, val end: LocalDate) : DateRange

    fun resolve(reference: LocalDate = LocalDate.today()): Pair<LocalDate, LocalDate>? = when (this) {
        AllTime -> null
        ThisMonth -> reference.startOfMonth() to reference.endOfMonth()
        LastMonth -> {
            val lastMonth = reference.startOfMonth().minus(1, DateTimeUnit.DAY)
            lastMonth.startOfMonth() to lastMonth.endOfMonth()
        }
        Last3Months -> reference.minus(3, DateTimeUnit.MONTH) to reference
        ThisYear -> LocalDate(reference.year, 1, 1) to LocalDate(reference.year, 12, 31)
        is Custom -> start to end
    }
}

enum class ExpenseSort {
    DateNewest,
    DateOldest,
    AmountHigh,
    AmountLow;

    val groupsByDate: Boolean get() = this == DateNewest || this == DateOldest
}

data class ExpenseFilter(
    val dateRange: DateRange = DateRange.AllTime,
    val categoryIds: Set<Long> = emptySet(),
    val nameQuery: String = "",
    val minAmountMinor: Long? = null,
    val maxAmountMinor: Long? = null,
) {
    val isActive: Boolean
        get() = dateRange !is DateRange.AllTime ||
            categoryIds.isNotEmpty() ||
            nameQuery.isNotBlank() ||
            minAmountMinor != null ||
            maxAmountMinor != null

    companion object {
        val Empty = ExpenseFilter()
    }
}
