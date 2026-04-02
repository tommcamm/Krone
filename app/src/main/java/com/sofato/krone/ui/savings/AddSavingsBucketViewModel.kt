package com.sofato.krone.ui.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.model.SavingsBucketType
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.savings.AddSavingsBucketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong

@HiltViewModel
class AddSavingsBucketViewModel @Inject constructor(
    private val addSavingsBucketUseCase: AddSavingsBucketUseCase,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _selectedType = MutableStateFlow(SavingsBucketType.GOAL)
    val selectedType: StateFlow<SavingsBucketType> = _selectedType.asStateFlow()

    private val _monthlyContributionInput = MutableStateFlow("")
    val monthlyContributionInput: StateFlow<String> = _monthlyContributionInput.asStateFlow()

    private val _targetAmountInput = MutableStateFlow("")
    val targetAmountInput: StateFlow<String> = _targetAmountInput.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private var currencyCode: String = "DKK"
    private var decimalPlaces: Int = 2

    init {
        viewModelScope.launch {
            currencyCode = userPreferencesRepository.homeCurrencyCode.first()
            val currency = currencyRepository.getCurrencyByCode(currencyCode)
            decimalPlaces = currency?.decimalPlaces ?: 2
        }
    }

    fun onLabelChanged(value: String) { _label.value = value }
    fun onTypeSelected(type: SavingsBucketType) { _selectedType.value = type }
    fun onMonthlyContributionChanged(value: String) {
        _monthlyContributionInput.value = value.filter { it.isDigit() || it == '.' || it == ',' }
    }
    fun onTargetAmountChanged(value: String) {
        _targetAmountInput.value = value.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun save() {
        val contributionText = _monthlyContributionInput.value.replace(",", ".")
        val contributionDouble = contributionText.toDoubleOrNull() ?: return
        if (contributionDouble <= 0) return
        if (_label.value.isBlank()) return

        val contributionMinor = (contributionDouble * 10.0.pow(decimalPlaces)).roundToLong()

        val targetText = _targetAmountInput.value.replace(",", ".")
        val targetMinor = targetText.toDoubleOrNull()?.let {
            if (it > 0) (it * 10.0.pow(decimalPlaces)).roundToLong() else null
        }

        viewModelScope.launch {
            _isSaving.value = true
            addSavingsBucketUseCase(
                SavingsBucket(
                    label = _label.value.trim(),
                    type = _selectedType.value,
                    currencyCode = currencyCode,
                    monthlyContributionMinor = contributionMinor,
                    targetAmountMinor = targetMinor,
                    deadline = null,
                    currentBalanceMinor = 0L,
                    isActive = true,
                    sortOrder = 0,
                )
            )
            _isSaving.value = false
            _events.emit(Event.Saved)
        }
    }

    sealed interface Event {
        data object Saved : Event
    }
}
