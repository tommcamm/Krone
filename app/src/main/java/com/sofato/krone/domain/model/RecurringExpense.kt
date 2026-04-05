package com.sofato.krone.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

data class RecurringExpense(
    val id: Long = 0,
    val amountMinor: Long,
    val currencyCode: String,
    val categoryId: Long,
    val label: String,
    val recurrenceRule: String,
    val nextDate: LocalDate,
    val isActive: Boolean,
    val createdAt: Instant,
    val dayOfMonth: Int? = null,
)
