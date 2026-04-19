package com.sofato.krone.domain.usecase.currency

import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.ExchangeRate
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

class ConvertAmountUseCaseTest {

    private val dkk = Currency("DKK", "Danish Krone", "kr.", 2, SymbolPosition.AFTER, true, 0)
    private val eur = Currency("EUR", "Euro", "€", 2, SymbolPosition.BEFORE, true, 1)
    private val jpy = Currency("JPY", "Japanese Yen", "¥", 0, SymbolPosition.BEFORE, true, 2)

    private lateinit var exchangeRateRepository: ExchangeRateRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var useCase: ConvertAmountUseCase

    @Before
    fun setUp() {
        exchangeRateRepository = mockk()
        userPreferencesRepository = mockk {
            every { homeCurrencyCode } returns flowOf("DKK")
        }
        currencyRepository = mockk {
            coEvery { getCurrencyByCode("DKK") } returns dkk
            coEvery { getCurrencyByCode("EUR") } returns eur
            coEvery { getCurrencyByCode("JPY") } returns jpy
        }
        useCase = ConvertAmountUseCase(
            exchangeRateRepository,
            userPreferencesRepository,
            currencyRepository,
        )
    }

    @Test
    fun `home-currency amount is returned unchanged without touching the rate store`() = runTest {
        val date = LocalDate(2026, 4, 19)

        val result = useCase(amountMinor = 15000, fromCode = "DKK", date = date)

        assertThat(result?.convertedAmountMinor).isEqualTo(15000)
        assertThat(result?.rateUsed).isEqualTo(1.0)
        coVerify(exactly = 0) { exchangeRateRepository.getRateForDate(any(), any(), any()) }
    }

    @Test
    fun `EUR to DKK applies the rate directly since both have 2 decimals`() = runTest {
        val date = LocalDate(2024, 1, 15)
        coEvery { exchangeRateRepository.getRateForDate("EUR", "DKK", date) } returns
            ExchangeRate("EUR", "DKK", 7.45, date, Instant.fromEpochSeconds(0), "frankfurter")

        // 100.00 EUR * 7.45 = 745.00 DKK
        val result = useCase(amountMinor = 10000, fromCode = "EUR", date = date)

        assertThat(result?.convertedAmountMinor).isEqualTo(74500)
        assertThat(result?.rateUsed).isEqualTo(7.45)
    }

    @Test
    fun `JPY to DKK scales by the decimal-place delta instead of under-shooting by 100x`() = runTest {
        // Regression: 5000 JPY at ~0.044 DKK/JPY previously produced "2.20 DKK" because the
        // code multiplied minor units by the rate without accounting for JPY having 0 decimals
        // and DKK having 2.
        val date = LocalDate(2026, 4, 19)
        coEvery { exchangeRateRepository.getRateForDate("JPY", "DKK", date) } returns
            ExchangeRate("JPY", "DKK", 0.044, date, Instant.fromEpochSeconds(0), "frankfurter")

        // 5000 JPY * 0.044 = 220.00 DKK → 22000 DKK minor units.
        val result = useCase(amountMinor = 5000, fromCode = "JPY", date = date)

        assertThat(result?.convertedAmountMinor).isEqualTo(22000)
        assertThat(result?.rateUsed).isEqualTo(0.044)
    }

    @Test
    fun `rate lookup uses the exact date the caller passed, not today`() = runTest {
        val pickedDate = LocalDate(2025, 11, 3)
        coEvery { exchangeRateRepository.getRateForDate("EUR", "DKK", pickedDate) } returns
            ExchangeRate("EUR", "DKK", 7.50, pickedDate, Instant.fromEpochSeconds(0), "frankfurter")

        useCase(amountMinor = 5000, fromCode = "EUR", date = pickedDate)

        coVerify(exactly = 1) { exchangeRateRepository.getRateForDate("EUR", "DKK", pickedDate) }
    }

    @Test
    fun `returns null when the rate is unavailable for the date`() = runTest {
        val date = LocalDate(2026, 4, 19)
        coEvery { exchangeRateRepository.getRateForDate("EUR", "DKK", date) } returns null

        val result = useCase(amountMinor = 10000, fromCode = "EUR", date = date)

        assertThat(result).isNull()
    }
}
