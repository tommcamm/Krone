package com.sofato.krone.ui.savings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.model.SavingsBucketType
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.usecase.savings.DeactivateSavingsBucketUseCase
import com.sofato.krone.domain.usecase.savings.UpdateSavingsBucketUseCase
import com.sofato.krone.ui.navigation.KroneDestination
import com.sofato.krone.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong

@HiltViewModel
class EditSavingsBucketViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savingsBucketRepository: SavingsBucketRepository,
    private val updateSavingsBucketUseCase: UpdateSavingsBucketUseCase,
    private val deactivateSavingsBucketUseCase: DeactivateSavingsBucketUseCase,
    private val currencyRepository: CurrencyRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<KroneDestination.EditSavingsBucket>()

    private val _bucket = MutableStateFlow<SavingsBucket?>(null)
    val bucket: StateFlow<SavingsBucket?> = _bucket.asStateFlow()

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _selectedType = MutableStateFlow(SavingsBucketType.GOAL)
    val selectedType: StateFlow<SavingsBucketType> = _selectedType.asStateFlow()

    private val _monthlyContributionInput = MutableStateFlow("")
    val monthlyContributionInput: StateFlow<String> = _monthlyContributionInput.asStateFlow()

    private val _targetAmountInput = MutableStateFlow("")
    val targetAmountInput: StateFlow<String> = _targetAmountInput.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private var decimalPlaces: Int = 2

    init {
        viewModelScope.launch {
            val loaded = savingsBucketRepository.getById(route.bucketId) ?: return@launch
            _bucket.value = loaded
            _label.value = loaded.label
            _selectedType.value = loaded.type
            val currency = currencyRepository.getCurrencyByCode(loaded.currencyCode)
            decimalPlaces = currency?.decimalPlaces ?: 2
            _monthlyContributionInput.value = CurrencyFormatter.formatPlain(loaded.monthlyContributionMinor, decimalPlaces)
            loaded.targetAmountMinor?.let {
                _targetAmountInput.value = CurrencyFormatter.formatPlain(it, decimalPlaces)
            }
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
        val original = _bucket.value ?: return
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
            updateSavingsBucketUseCase(
                original.copy(
                    label = _label.value.trim(),
                    type = _selectedType.value,
                    monthlyContributionMinor = contributionMinor,
                    targetAmountMinor = targetMinor,
                )
            )
            _events.emit(Event.Saved)
        }
    }

    fun deactivate() {
        viewModelScope.launch {
            _bucket.value?.let {
                deactivateSavingsBucketUseCase(it.id)
                _events.emit(Event.Deleted)
            }
        }
    }

    sealed interface Event {
        data object Saved : Event
        data object Deleted : Event
    }
}
