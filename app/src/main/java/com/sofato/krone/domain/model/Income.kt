package com.sofato.krone.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

data class Income(
    val id: Long = 0,
    val amountMinor: Long,
    val currencyCode: String,
    val label: String,
    val isRecurring: Boolean,
    val recurrenceRule: String?,
    val startDate: LocalDate,
    val createdAt: Instant,
)
