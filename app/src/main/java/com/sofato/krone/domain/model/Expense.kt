package com.sofato.krone.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.math.pow

data class Expense(
    val id: Long = 0,
    val amount: Long,
    val currency: Currency,
    val homeAmount: Long,
    val exchangeRateUsed: Double,
    val category: Category,
    val note: String?,
    val date: LocalDate,
    val createdAt: Instant,
    val isRecurringInstance: Boolean = false,
    val recurringExpenseId: Long? = null,
) {
    val displayAmount: Double
        get() = amount.toDouble() / 10.0.pow(currency.decimalPlaces)

    val displayHomeAmount: Double
        get() = homeAmount.toDouble() / 10.0.pow(currency.decimalPlaces)
}
