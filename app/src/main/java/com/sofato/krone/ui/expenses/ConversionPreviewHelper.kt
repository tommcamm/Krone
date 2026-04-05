package com.sofato.krone.ui.expenses

import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Defaults
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import com.sofato.krone.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.days

enum class RateFreshness { FRESH, STALE, UNAVAILABLE }

class ConversionPreviewHelper(
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val convertAmountUseCase: ConvertAmountUseCase,
    private val exchangeRateRepository: ExchangeRateRepository,
) {
    private val _convertedAmountText = MutableStateFlow<String?>(null)
    val convertedAmountText: StateFlow<String?> = _convertedAmountText.asStateFlow()

    private val _rateFreshness = MutableStateFlow(RateFreshness.UNAVAILABLE)
    val rateFreshness: StateFlow<RateFreshness> = _rateFreshness.asStateFlow()

    private val _isForeignCurrency = MutableStateFlow(false)
    val isForeignCurrency: StateFlow<Boolean> = _isForeignCurrency.asStateFlow()

    @OptIn(FlowPreview::class)
    fun startObserving(
        scope: CoroutineScope,
        amountInput: StateFlow<String>,
        selectedCurrency: StateFlow<Currency?>,
    ) {
        // Debounced conversion preview
        scope.launch {
            combine(amountInput.debounce(Defaults.CONVERSION_DEBOUNCE_MS), selectedCurrency) { amount, currency ->
                amount to currency
            }.collect { (amountText, currency) ->
                updateConversionPreview(amountText, currency)
            }
        }

        // Rate freshness
        scope.launch {
            selectedCurrency.collect { currency ->
                val homeCode = userPreferencesRepository.homeCurrencyCode.first()
                val isForeign = currency != null && currency.code != homeCode
                _isForeignCurrency.value = isForeign
                if (isForeign) {
                    updateRateFreshness()
                }
            }
        }
    }

    private suspend fun updateConversionPreview(amountText: String, currency: Currency?) {
        if (currency == null) {
            _convertedAmountText.value = null
            return
        }
        val homeCode = userPreferencesRepository.homeCurrencyCode.first()
        if (currency.code == homeCode) {
            _convertedAmountText.value = null
            return
        }
        val parsed = amountText.replace(",", ".").toDoubleOrNull()
        if (parsed == null || parsed <= 0) {
            _convertedAmountText.value = null
            return
        }
        val amountMinor = (parsed * 10.0.pow(currency.decimalPlaces)).roundToLong()
        val result = convertAmountUseCase(amountMinor, currency.code)
        if (result != null) {
            val homeCurrency = currencyRepository.getCurrencyByCode(homeCode)
            if (homeCurrency != null) {
                _convertedAmountText.value = "\u2248 ${CurrencyFormatter.formatDisplay(result.convertedAmountMinor, homeCurrency)}"
            }
        } else {
            _convertedAmountText.value = null
        }
    }

    private suspend fun updateRateFreshness() {
        val fetchTime = exchangeRateRepository.getLatestFetchTime()
        _rateFreshness.value = when {
            fetchTime == null -> RateFreshness.UNAVAILABLE
            Clock.System.now() - fetchTime < 1.days -> RateFreshness.FRESH
            else -> RateFreshness.STALE
        }
    }
}
