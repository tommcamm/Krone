package com.sofato.krone.ui.expenses

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.currency.ConversionResult
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversionPreviewHelperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val eur = Currency("EUR", "Euro", "€", 2, SymbolPosition.BEFORE, true, 0)
    private val dkk = Currency("DKK", "Danish Krone", "kr.", 2, SymbolPosition.AFTER, true, 1)

    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var convertAmountUseCase: ConvertAmountUseCase
    private lateinit var exchangeRateRepository: ExchangeRateRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        currencyRepository = mockk {
            coEvery { getCurrencyByCode("DKK") } returns dkk
        }
        userPreferencesRepository = mockk {
            every { homeCurrencyCode } returns flowOf("DKK")
        }
        convertAmountUseCase = mockk()
        exchangeRateRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `preview refreshes when selected date changes`() = runTest {
        // Two different rates for two different dates.
        coEvery { convertAmountUseCase(10000, "EUR", LocalDate(2026, 4, 19)) } returns
            ConversionResult(convertedAmountMinor = 74600, rateUsed = 7.46)
        coEvery { convertAmountUseCase(10000, "EUR", LocalDate(2024, 1, 15)) } returns
            ConversionResult(convertedAmountMinor = 74000, rateUsed = 7.40)

        val helper = ConversionPreviewHelper(
            currencyRepository, userPreferencesRepository, convertAmountUseCase, exchangeRateRepository,
        )
        val amountInput = MutableStateFlow("100")
        val selectedCurrency = MutableStateFlow<Currency?>(eur)
        val selectedDate = MutableStateFlow(LocalDate(2026, 4, 19))

        helper.startObserving(TestScope(testDispatcher), amountInput, selectedCurrency, selectedDate)

        helper.convertedAmountText.test {
            // Initial state before debounce elapses.
            assertThat(awaitItem()).isNull()

            advanceTimeBy(600)
            assertThat(awaitItem()).contains("746")

            // Date change → preview re-runs with the new date's rate.
            selectedDate.value = LocalDate(2024, 1, 15)
            advanceTimeBy(600)
            assertThat(awaitItem()).contains("740")
        }

        coVerify { convertAmountUseCase(10000, "EUR", LocalDate(2026, 4, 19)) }
        coVerify { convertAmountUseCase(10000, "EUR", LocalDate(2024, 1, 15)) }
    }

    @Test
    fun `preview passes the current selected date to the convert use case`() = runTest {
        val date = LocalDate(2025, 7, 4)
        coEvery { convertAmountUseCase(5000, "EUR", date) } returns
            ConversionResult(convertedAmountMinor = 37300, rateUsed = 7.46)

        val helper = ConversionPreviewHelper(
            currencyRepository, userPreferencesRepository, convertAmountUseCase, exchangeRateRepository,
        )
        val amountInput = MutableStateFlow("50")
        val selectedCurrency = MutableStateFlow<Currency?>(eur)
        val selectedDate = MutableStateFlow(date)

        helper.startObserving(TestScope(testDispatcher), amountInput, selectedCurrency, selectedDate)
        advanceTimeBy(600)

        coVerify(exactly = 1) { convertAmountUseCase(5000, "EUR", date) }
    }
}
