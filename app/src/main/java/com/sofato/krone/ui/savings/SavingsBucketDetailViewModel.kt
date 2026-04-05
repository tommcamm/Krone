package com.sofato.krone.ui.savings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.model.SavingsContribution
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.savings.GetSavingsBucketDetailUseCase
import com.sofato.krone.ui.navigation.KroneDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SavingsBucketDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSavingsBucketDetailUseCase: GetSavingsBucketDetailUseCase,
    private val savingsBucketRepository: SavingsBucketRepository,
    currencyRepository: CurrencyRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<KroneDestination.SavingsBucketDetail>()

    private val _bucket = MutableStateFlow<SavingsBucket?>(null)
    val bucket: StateFlow<SavingsBucket?> = _bucket.asStateFlow()

    private val _contributions = MutableStateFlow<List<SavingsContribution>>(emptyList())
    val contributions: StateFlow<List<SavingsContribution>> = _contributions.asStateFlow()

    val homeCurrency: StateFlow<Currency?> =
        userPreferencesRepository.homeCurrencyCode
            .flatMapLatest { code ->
                currencyRepository.getEnabledCurrencies()
                    .combine(flowOf(code)) { currencies, homeCode ->
                        currencies.find { it.code == homeCode }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            getSavingsBucketDetailUseCase(route.bucketId).collect { detail ->
                if (detail != null) {
                    _bucket.value = detail.bucket
                    _contributions.value = detail.contributions
                }
            }
        }
    }

    fun updateBalance(newBalanceMinor: Long) {
        viewModelScope.launch {
            savingsBucketRepository.updateBalance(
                id = route.bucketId,
                balance = newBalanceMinor,
                updatedAt = Clock.System.now(),
            )
        }
    }
}
