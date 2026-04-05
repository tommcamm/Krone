package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.CurrencyDao
import com.sofato.krone.data.db.dao.ExchangeRateDao
import com.sofato.krone.data.db.entity.ExchangeRateEntity
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.network.FrankfurterApi
import com.sofato.krone.domain.model.ExchangeRate
import com.sofato.krone.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Instant
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val exchangeRateDao: ExchangeRateDao,
    private val currencyDao: CurrencyDao,
    private val frankfurterApi: FrankfurterApi,
) : ExchangeRateRepository {

    override suspend fun getRate(from: String, to: String): ExchangeRate? {
        if (from == to) return ExchangeRate(from, to, 1.0, Clock.System.now(), "identity")
        return exchangeRateDao.getLatestRate(from, to)?.toDomain()
    }

    override suspend fun refreshRates(): Result<Unit> = runCatching {
        val response = frankfurterApi.getLatestRates()
        val now = Clock.System.now()
        val enabledCodes = currencyDao.getEnabledCurrencies().first().map { it.code }.toSet()

        // Frankfurter returns EUR-based rates. Build cross-rate pairs for all enabled currencies.
        // Include EUR itself with rate 1.0
        val eurRates = buildMap {
            put("EUR", 1.0)
            putAll(response.rates)
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
                        fetchedAt = now,
                        source = "frankfurter",
                    )
                )
            }
        }
        exchangeRateDao.insertRates(entities)
    }

    override suspend fun getLatestFetchTime(): Instant? {
        return exchangeRateDao.getLatestFetchTime()
    }
}
