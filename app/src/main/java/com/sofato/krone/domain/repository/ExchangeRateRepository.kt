package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.ExchangeRate
import kotlinx.datetime.Instant

interface ExchangeRateRepository {
    suspend fun getRate(from: String, to: String): ExchangeRate?
    suspend fun refreshRates(): Result<Unit>
    suspend fun getLatestFetchTime(): Instant?
}
