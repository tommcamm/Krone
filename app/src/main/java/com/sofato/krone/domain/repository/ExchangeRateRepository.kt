package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.ExchangeRate
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

interface ExchangeRateRepository {
    /**
     * Returns the rate applicable at [date] — nearest quote on-or-before the date, falling
     * back to a historical fetch from the network if a hole exists, and finally to the
     * nearest quote after the date. Use this whenever locking a rate to a transaction date.
     */
    suspend fun getRateForDate(from: String, to: String, date: LocalDate): ExchangeRate?

    /** Latest locally-cached rate. Use for UI previews only, not for locking transactions. */
    suspend fun getLatestRate(from: String, to: String): ExchangeRate?

    suspend fun refreshRates(): Result<Unit>
    suspend fun getLatestFetchTime(): Instant?
}
