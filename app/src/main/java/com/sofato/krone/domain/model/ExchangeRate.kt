package com.sofato.krone.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate

data class ExchangeRate(
    val baseCode: String,
    val targetCode: String,
    val rate: Double,
    val rateDate: LocalDate,
    val fetchedAt: Instant,
    val source: String,
)
