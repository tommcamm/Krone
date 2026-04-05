package com.sofato.krone.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Defaults
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.ExchangeRate
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Instant
import javax.inject.Inject

data class CurrencyWithRate(
    val currency: Currency,
    val rateToHome: ExchangeRate?,
    val isHome: Boolean,
)

@HiltViewModel
class CurrencySettingsViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _homeCurrencyCode = MutableStateFlow(Defaults.HOME_CURRENCY_CODE)
    val homeCurrencyCode: StateFlow<String> = _homeCurrencyCode.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Instant?>(null)
    val lastSyncTime: StateFlow<Instant?> = _lastSyncTime.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Tracks which currency code is currently being toggled (for per-row loading state)
    private val _togglingCode = MutableStateFlow<String?>(null)
    val togglingCode: StateFlow<String?> = _togglingCode.asStateFlow()

    private val _events = MutableSharedFlow<CurrencySettingsEvent>()
    val events = _events.asSharedFlow()

    val currencies: StateFlow<List<CurrencyWithRate>> =
        combine(
            currencyRepository.getAllCurrencies(),
            _homeCurrencyCode,
            _lastSyncTime,
        ) { allCurrencies, homeCode, _ ->
            allCurrencies.map { currency ->
                CurrencyWithRate(
                    currency = currency,
                    rateToHome = if (currency.code == homeCode) null
                    else exchangeRateRepository.getRate(currency.code, homeCode),
                    isHome = currency.code == homeCode,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _homeCurrencyCode.value = userPreferencesRepository.homeCurrencyCode.first()
            _lastSyncTime.value = exchangeRateRepository.getLatestFetchTime()
        }
    }

    fun onToggleCurrency(code: String, enabled: Boolean) {
        if (!enabled) {
            // Disabling doesn't need a rate check
            viewModelScope.launch {
                currencyRepository.setEnabled(code, false)
            }
            return
        }

        // Enabling: fetch rates first, only enable if rates are available.
        viewModelScope.launch {
            _togglingCode.value = code
            val result = exchangeRateRepository.refreshRates()
            if (result.isSuccess) {
                currencyRepository.setEnabled(code, true)
                _lastSyncTime.value = exchangeRateRepository.getLatestFetchTime()
            } else {
                _events.emit(CurrencySettingsEvent.RateFetchFailed(code))
            }
            _togglingCode.value = null
        }
    }

    fun onRefreshRates() {
        _isSyncing.value = true
        viewModelScope.launch {
            val result = exchangeRateRepository.refreshRates()
            if (result.isFailure) {
                _events.emit(CurrencySettingsEvent.RefreshFailed)
            }
            _lastSyncTime.value = exchangeRateRepository.getLatestFetchTime()
            _isSyncing.value = false
        }
    }

    sealed interface CurrencySettingsEvent {
        data class RateFetchFailed(val currencyCode: String) : CurrencySettingsEvent
        data object RefreshFailed : CurrencySettingsEvent
    }
}
