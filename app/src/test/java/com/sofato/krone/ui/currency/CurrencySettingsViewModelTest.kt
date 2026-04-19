package com.sofato.krone.ui.currency

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.ExchangeRate
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencySettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun currency(code: String, enabled: Boolean, sortOrder: Int = 0) = Currency(
        code = code,
        name = code,
        symbol = code,
        decimalPlaces = 2,
        symbolPosition = SymbolPosition.BEFORE,
        isEnabled = enabled,
        sortOrder = sortOrder,
    )

    /**
     * Fake repository whose `setEnabled` mutates the flow, so `getAllCurrencies()` emits updates
     * like the real Room-backed impl.
     */
    private class FakeCurrencyRepository(initial: List<Currency>) : CurrencyRepository {
        private val state = MutableStateFlow(initial)
        val currentEnabledCodes: List<String>
            get() = state.value.filter { it.isEnabled }.map { it.code }
        override fun getAllCurrencies(): Flow<List<Currency>> = state
        override fun getEnabledCurrencies(): Flow<List<Currency>> = state
        override suspend fun getCurrencyByCode(code: String): Currency? =
            state.value.firstOrNull { it.code == code }
        override suspend fun setEnabled(code: String, enabled: Boolean) {
            state.value = state.value.map {
                if (it.code == code) it.copy(isEnabled = enabled) else it
            }
        }
        fun isEnabled(code: String): Boolean =
            state.value.first { it.code == code }.isEnabled
    }

    /**
     * Fake that mirrors the real impl's key behavior: `refreshRates()` only persists rates
     * for currencies *currently enabled* in the companion [CurrencyRepository]. This is the
     * constraint that made the original ordering bug observable — if a currency is toggled on
     * after refresh, no rate gets persisted for it.
     */
    private class FakeExchangeRateRepository(
        private val currencies: FakeCurrencyRepository,
        private val homeCode: String,
        var shouldFail: Boolean = false,
    ) : ExchangeRateRepository {
        private val rates = mutableMapOf<Pair<String, String>, ExchangeRate>()
        private var latestFetch: Instant? = null
        var refreshCount = 0
            private set

        override suspend fun getRate(from: String, to: String): ExchangeRate? {
            if (from == to) return ExchangeRate(from, to, 1.0, Clock.System.now(), "identity")
            return rates[from to to]
        }

        override suspend fun refreshRates(): Result<Unit> {
            refreshCount++
            if (shouldFail) return Result.failure(RuntimeException("network"))
            val now = Clock.System.now()
            for (code in currencies.currentEnabledCodes) {
                if (code == homeCode) continue
                rates[code to homeCode] =
                    ExchangeRate(code, homeCode, 7.5, now, "fake")
            }
            latestFetch = now
            return Result.success(Unit)
        }

        override suspend fun getLatestFetchTime(): Instant? = latestFetch
    }

    private lateinit var userPrefs: UserPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userPrefs = mockk {
            every { homeCurrencyCode } returns flowOf("DKK")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `enabling a currency surfaces its rate without manual refresh`() = runTest {
        // Regression: previously refreshRates() ran BEFORE setEnabled(), so the newly-toggled
        // currency was excluded from the enabled-codes set used to build cross-rate pairs.
        // The rate would only appear after a second, manual refresh.
        val currencyRepo = FakeCurrencyRepository(
            listOf(
                currency("DKK", enabled = true, sortOrder = 0),
                currency("USD", enabled = false, sortOrder = 1),
            ),
        )
        val rateRepo = FakeExchangeRateRepository(currencyRepo, homeCode = "DKK")

        val viewModel = CurrencySettingsViewModel(currencyRepo, rateRepo, userPrefs)

        viewModel.currencies.test {
            // Drain emissions until USD is observed in its initial (disabled, no rate) state.
            // stateIn's initial empty-list emission may or may not arrive depending on timing.
            var initialUsd: CurrencyWithRate? = null
            while (initialUsd == null) {
                initialUsd = awaitItem().firstOrNull { it.currency.code == "USD" }
            }
            assertThat(initialUsd.currency.isEnabled).isFalse()
            assertThat(initialUsd.rateToHome).isNull()

            viewModel.onToggleCurrency("USD", enabled = true)

            // Skip intermediate emissions; land on the state where USD is enabled AND has a rate.
            val usdWithRate = expectMostRecentItem().first { it.currency.code == "USD" }
            assertThat(usdWithRate.currency.isEnabled).isTrue()
            assertThat(usdWithRate.rateToHome).isNotNull()
            assertThat(usdWithRate.rateToHome!!.rate).isEqualTo(7.5)
        }

        assertThat(rateRepo.refreshCount).isEqualTo(1)
    }

    @Test
    fun `failed refresh reverts the enable so UI stays consistent`() = runTest {
        // Extra coverage: the fix enables optimistically, so it must undo the enable
        // when the network refresh fails. Otherwise the currency would appear enabled
        // with no rate and no way to recover short of retrying the toggle.
        val currencyRepo = FakeCurrencyRepository(
            listOf(
                currency("DKK", enabled = true, sortOrder = 0),
                currency("USD", enabled = false, sortOrder = 1),
            ),
        )
        val rateRepo = FakeExchangeRateRepository(currencyRepo, homeCode = "DKK", shouldFail = true)

        val viewModel = CurrencySettingsViewModel(currencyRepo, rateRepo, userPrefs)

        viewModel.events.test {
            viewModel.onToggleCurrency("USD", enabled = true)
            val event = awaitItem()
            assertThat(event)
                .isInstanceOf(CurrencySettingsViewModel.CurrencySettingsEvent.RateFetchFailed::class.java)
        }

        assertThat(currencyRepo.isEnabled("USD")).isFalse()
    }

    @Test
    fun `disabling a currency does not trigger a rate refresh`() = runTest {
        val currencyRepo = FakeCurrencyRepository(
            listOf(
                currency("DKK", enabled = true, sortOrder = 0),
                currency("USD", enabled = true, sortOrder = 1),
            ),
        )
        val rateRepo = FakeExchangeRateRepository(currencyRepo, homeCode = "DKK")

        val viewModel = CurrencySettingsViewModel(currencyRepo, rateRepo, userPrefs)

        viewModel.onToggleCurrency("USD", enabled = false)

        assertThat(currencyRepo.isEnabled("USD")).isFalse()
        assertThat(rateRepo.refreshCount).isEqualTo(0)
    }
}
