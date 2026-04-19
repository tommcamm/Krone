package com.sofato.krone.data.repository

import com.google.common.truth.Truth.assertThat
import com.sofato.krone.data.db.dao.CurrencyDao
import com.sofato.krone.data.db.dao.ExchangeRateDao
import com.sofato.krone.data.db.entity.CurrencyEntity
import com.sofato.krone.data.db.entity.ExchangeRateEntity
import com.sofato.krone.data.network.FrankfurterApi
import com.sofato.krone.data.network.dto.FrankfurterResponse
import com.sofato.krone.domain.model.SymbolPosition
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

class ExchangeRateRepositoryImplTest {

    private lateinit var exchangeRateDao: ExchangeRateDao
    private lateinit var currencyDao: CurrencyDao
    private lateinit var frankfurterApi: FrankfurterApi
    private lateinit var repo: ExchangeRateRepositoryImpl

    @Before
    fun setUp() {
        exchangeRateDao = mockk(relaxed = true)
        currencyDao = mockk {
            every { getEnabledCurrencies() } returns flowOf(
                listOf(
                    CurrencyEntity("EUR", "Euro", "€", 2, SymbolPosition.BEFORE, true, 0),
                    CurrencyEntity("DKK", "Danish Krone", "kr.", 2, SymbolPosition.AFTER, true, 1),
                )
            )
        }
        frankfurterApi = mockk()
        repo = ExchangeRateRepositoryImpl(exchangeRateDao, currencyDao, frankfurterApi)
    }

    @Test
    fun `same-currency lookup returns identity rate without hitting DAO or API`() = runTest {
        val result = repo.getRateForDate("DKK", "DKK", LocalDate(2026, 4, 19))

        assertThat(result).isNotNull()
        assertThat(result!!.rate).isEqualTo(1.0)
        coVerify(exactly = 0) { exchangeRateDao.getRateOnOrBefore(any(), any(), any()) }
        coVerify(exactly = 0) { frankfurterApi.getHistoricalRates(any()) }
    }

    @Test
    fun `exact-date rate is returned directly from DAO`() = runTest {
        val date = LocalDate(2026, 4, 19)
        coEvery { exchangeRateDao.getRateOnOrBefore("EUR", "DKK", date) } returns ExchangeRateEntity(
            id = 1, baseCode = "EUR", targetCode = "DKK", rate = 7.45, rateDate = date,
            fetchedAt = Instant.fromEpochSeconds(0), source = "frankfurter",
        )

        val result = repo.getRateForDate("EUR", "DKK", date)

        assertThat(result).isNotNull()
        assertThat(result!!.rate).isEqualTo(7.45)
        assertThat(result.rateDate).isEqualTo(date)
        coVerify(exactly = 0) { frankfurterApi.getHistoricalRates(any()) }
    }

    @Test
    fun `earlier stored date is returned when exact date missing (nearest-on-or-before)`() = runTest {
        val queryDate = LocalDate(2026, 4, 19)
        val earlierDate = LocalDate(2026, 4, 17) // e.g., Friday when querying Sunday
        coEvery { exchangeRateDao.getRateOnOrBefore("EUR", "DKK", queryDate) } returns ExchangeRateEntity(
            id = 1, baseCode = "EUR", targetCode = "DKK", rate = 7.40, rateDate = earlierDate,
            fetchedAt = Instant.fromEpochSeconds(0), source = "frankfurter",
        )

        val result = repo.getRateForDate("EUR", "DKK", queryDate)

        assertThat(result!!.rateDate).isEqualTo(earlierDate)
        assertThat(result.rate).isEqualTo(7.40)
        coVerify(exactly = 0) { frankfurterApi.getHistoricalRates(any()) }
    }

    @Test
    fun `no local rate at-or-before triggers historical fetch and returns the backfilled rate`() = runTest {
        val queryDate = LocalDate(2024, 1, 15)
        val quoteDate = LocalDate(2024, 1, 15)

        // First lookup: nothing. After persist: row exists.
        coEvery {
            exchangeRateDao.getRateOnOrBefore("EUR", "DKK", queryDate)
        } returnsMany listOf(
            null,
            ExchangeRateEntity(
                id = 1, baseCode = "EUR", targetCode = "DKK", rate = 7.46, rateDate = quoteDate,
                fetchedAt = Instant.fromEpochSeconds(0), source = "frankfurter",
            ),
        )
        coEvery { frankfurterApi.getHistoricalRates(queryDate) } returns FrankfurterResponse(
            amount = 1.0,
            base = "EUR",
            date = quoteDate.toString(),
            rates = mapOf("DKK" to 7.46),
        )

        val captured = slot<List<ExchangeRateEntity>>()
        coEvery { exchangeRateDao.insertRates(capture(captured)) } returns Unit

        val result = repo.getRateForDate("EUR", "DKK", queryDate)

        assertThat(result).isNotNull()
        assertThat(result!!.rate).isEqualTo(7.46)
        coVerify(exactly = 1) { frankfurterApi.getHistoricalRates(queryDate) }
        // Both cross-pairs (EUR->DKK and DKK->EUR) should have been persisted with the quote date.
        assertThat(captured.captured.map { it.rateDate }.distinct()).containsExactly(quoteDate)
        assertThat(captured.captured.map { "${it.baseCode}->${it.targetCode}" })
            .containsAtLeast("EUR->DKK", "DKK->EUR")
    }

    @Test
    fun `historical fetch failure falls back to nearest-after rate`() = runTest {
        val queryDate = LocalDate(2024, 1, 15)
        val laterDate = LocalDate(2024, 2, 1)

        coEvery { exchangeRateDao.getRateOnOrBefore("EUR", "DKK", queryDate) } returns null
        coEvery { frankfurterApi.getHistoricalRates(queryDate) } throws RuntimeException("offline")
        coEvery { exchangeRateDao.getRateOnOrAfter("EUR", "DKK", queryDate) } returns ExchangeRateEntity(
            id = 2, baseCode = "EUR", targetCode = "DKK", rate = 7.50, rateDate = laterDate,
            fetchedAt = Instant.fromEpochSeconds(0), source = "frankfurter",
        )

        val result = repo.getRateForDate("EUR", "DKK", queryDate)

        assertThat(result).isNotNull()
        assertThat(result!!.rateDate).isEqualTo(laterDate)
        assertThat(result.rate).isEqualTo(7.50)
    }

    @Test
    fun `returns null when no local rate and no historical data and no nearest-after`() = runTest {
        val queryDate = LocalDate(1900, 1, 1)

        coEvery { exchangeRateDao.getRateOnOrBefore("EUR", "DKK", queryDate) } returns null
        coEvery { frankfurterApi.getHistoricalRates(queryDate) } throws RuntimeException("no data")
        coEvery { exchangeRateDao.getRateOnOrAfter("EUR", "DKK", queryDate) } returns null

        val result = repo.getRateForDate("EUR", "DKK", queryDate)

        assertThat(result).isNull()
    }

    @Test
    fun `refreshRates uses Frankfurter response date as rateDate, not today`() = runTest {
        val quoteDate = LocalDate(2026, 4, 17) // Frankfurter's Friday quote returned on a Sunday call
        coEvery { frankfurterApi.getLatestRates() } returns FrankfurterResponse(
            amount = 1.0,
            base = "EUR",
            date = quoteDate.toString(),
            rates = mapOf("DKK" to 7.46),
        )

        val captured = slot<List<ExchangeRateEntity>>()
        coEvery { exchangeRateDao.insertRates(capture(captured)) } returns Unit

        val result = repo.refreshRates()

        assertThat(result.isSuccess).isTrue()
        assertThat(captured.captured).isNotEmpty()
        assertThat(captured.captured.map { it.rateDate }.distinct()).containsExactly(quoteDate)
    }
}
