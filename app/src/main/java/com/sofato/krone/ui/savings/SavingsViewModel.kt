package com.sofato.krone.ui.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.savings.DeactivateSavingsBucketUseCase
import com.sofato.krone.domain.usecase.savings.GetActiveSavingsBucketsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SavingsViewModel @Inject constructor(
    getActiveSavingsBucketsUseCase: GetActiveSavingsBucketsUseCase,
    private val deactivateSavingsBucketUseCase: DeactivateSavingsBucketUseCase,
    savingsBucketRepository: SavingsBucketRepository,
    currencyRepository: CurrencyRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val buckets: StateFlow<List<SavingsBucket>> =
        getActiveSavingsBucketsUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMonthlyContribution: StateFlow<Long> =
        savingsBucketRepository.getTotalMonthlyContributionsMinor()
            .map { it ?: 0L }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val homeCurrency: StateFlow<Currency?> =
        userPreferencesRepository.homeCurrencyCode
            .flatMapLatest { code ->
                currencyRepository.getEnabledCurrencies()
                    .combine(flowOf(code)) { currencies, homeCode ->
                        currencies.find { it.code == homeCode }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deactivateBucket(id: Long) {
        viewModelScope.launch {
            deactivateSavingsBucketUseCase(id)
        }
    }
}
