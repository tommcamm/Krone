package com.sofato.krone.domain.model

import kotlin.time.Instant

data class ExchangeRate(
    val baseCode: String,
    val targetCode: String,
    val rate: Double,
    val fetchedAt: Instant,
    val source: String,
)
