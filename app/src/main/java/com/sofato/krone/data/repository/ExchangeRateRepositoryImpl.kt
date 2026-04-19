package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.CurrencyDao
import com.sofato.krone.data.db.dao.ExchangeRateDao
import com.sofato.krone.data.db.entity.ExchangeRateEntity
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.network.FrankfurterApi
import com.sofato.krone.domain.model.ExchangeRate
import com.sofato.krone.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val exchangeRateDao: ExchangeRateDao,
    private val currencyDao: CurrencyDao,
    private val frankfurterApi: FrankfurterApi,
) : ExchangeRateRepository {

    override suspend fun getRateForDate(from: String, to: String, date: LocalDate): ExchangeRate? {
        if (from == to) {
            return ExchangeRate(from, to, 1.0, date, Clock.System.now(), "identity")
        }

        exchangeRateDao.getRateOnOrBefore(from, to, date)?.let { return it.toDomain() }

        // No local rate at/before the target date — try to backfill from Frankfurter.
        runCatching { frankfurterApi.getHistoricalRates(date) }.onSuccess { response ->
            val quoteDate = runCatching { LocalDate.parse(response.date) }.getOrNull() ?: date
            persistRatesFromResponse(response.rates, quoteDate)
            exchangeRateDao.getRateOnOrBefore(from, to, date)?.let { return it.toDomain() }
        }

        // Last resort: nearest-after. Better than failing the transaction entirely; rates
        // between close dates differ only marginally.
        return exchangeRateDao.getRateOnOrAfter(from, to, date)?.toDomain()
    }

    override suspend fun getLatestRate(from: String, to: String): ExchangeRate? {
        if (from == to) return ExchangeRate(from, to, 1.0, LocalDate.fromEpochDays(0), Clock.System.now(), "identity")
        return exchangeRateDao.getLatestRate(from, to)?.toDomain()
    }

    override suspend fun refreshRates(): Result<Unit> = runCatching {
        val response = frankfurterApi.getLatestRates()
        val quoteDate = runCatching { LocalDate.parse(response.date) }.getOrNull()
            ?: error("Frankfurter response missing date")
        persistRatesFromResponse(response.rates, quoteDate)
    }

    override suspend fun getLatestFetchTime(): Instant? {
        return exchangeRateDao.getLatestFetchTime()
    }

    private suspend fun persistRatesFromResponse(
        eurBasedRates: Map<String, Double>,
        rateDate: LocalDate,
    ) {
        val now = Clock.System.now()
        val enabledCodes = currencyDao.getEnabledCurrencies().first().map { it.code }.toSet()

        // Frankfurter returns EUR-based rates. Build cross-rate pairs for enabled currencies.
        val eurRates = buildMap {
            put("EUR", 1.0)
            putAll(eurBasedRates)
        }

        val entities = mutableListOf<ExchangeRateEntity>()
        for (from in enabledCodes) {
            val eurToFrom = eurRates[from] ?: continue
            for (to in enabledCodes) {
                if (from == to) continue
                val eurToTo = eurRates[to] ?: continue
                // rate(from -> to) = eurToTo / eurToFrom
                val crossRate = eurToTo / eurToFrom
                entities.add(
                    ExchangeRateEntity(
                        baseCode = from,
                        targetCode = to,
                        rate = crossRate,
                        rateDate = rateDate,
                        fetchedAt = now,
                        source = "frankfurter",
                    )
                )
            }
        }
        if (entities.isNotEmpty()) {
            exchangeRateDao.insertRates(entities)
        }
    }
}
