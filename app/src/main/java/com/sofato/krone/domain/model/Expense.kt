package com.sofato.krone.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
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
)
