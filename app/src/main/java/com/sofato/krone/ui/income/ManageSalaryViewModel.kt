package com.sofato.krone.ui.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Income
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.IncomeRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.model.Defaults
import com.sofato.krone.util.CurrencyFormatter
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
class ManageSalaryViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRepository: CurrencyRepository,
) : ViewModel() {

    private val _incomes = MutableStateFlow<List<Income>>(emptyList())
    val incomes: StateFlow<List<Income>> = _incomes.asStateFlow()

    private val _editingIncome = MutableStateFlow<Income?>(null)
    val editingIncome: StateFlow<Income?> = _editingIncome.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _labelInput = MutableStateFlow("")
    val labelInput: StateFlow<String> = _labelInput.asStateFlow()

    private val _incomeDay = MutableStateFlow(1)
    val incomeDay: StateFlow<Int> = _incomeDay.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private var currencyCode: String = Defaults.HOME_CURRENCY_CODE
    private var decimalPlaces: Int = Defaults.DECIMAL_PLACES

    val homeCurrencyCode: StateFlow<String> =
        userPreferencesRepository.homeCurrencyCode
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Defaults.HOME_CURRENCY_CODE)

    init {
        viewModelScope.launch {
            currencyCode = userPreferencesRepository.homeCurrencyCode.first()
            val currency = currencyRepository.getCurrencyByCode(currencyCode)
            decimalPlaces = currency?.decimalPlaces ?: 2
            _incomeDay.value = userPreferencesRepository.incomeDay.first()

            incomeRepository.getRecurringIncome().collect { list ->
                _incomes.value = list
                // Auto-select the first income for editing if not already editing
                if (_editingIncome.value == null && list.isNotEmpty()) {
                    startEditing(list.first())
                }
            }
        }
    }

    fun startEditing(income: Income) {
        _editingIncome.value = income
        _amountInput.value = CurrencyFormatter.formatPlain(income.amountMinor, decimalPlaces)
        _labelInput.value = income.label
    }

    fun onAmountChanged(value: String) {
        _amountInput.value = value.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun onLabelChanged(value: String) {
        _labelInput.value = value
    }

    fun onIncomeDayChanged(day: Int) {
        _incomeDay.value = day.coerceIn(1, 31)
    }

    fun save() {
        val editing = _editingIncome.value ?: return
        val amountText = _amountInput.value.replace(",", ".")
        val amountDouble = amountText.toDoubleOrNull() ?: return
        if (amountDouble <= 0 || _labelInput.value.isBlank()) return

        val amountMinor = (amountDouble * 10.0.pow(decimalPlaces)).roundToLong()

        viewModelScope.launch {
            _isSaving.value = true
            try {
                incomeRepository.updateIncome(
                    editing.copy(
                        amountMinor = amountMinor,
                        label = _labelInput.value.trim(),
                    )
                )
                userPreferencesRepository.setIncomeDay(_incomeDay.value)
                _events.emit(Event.Saved)
            } catch (_: Exception) {
                _events.emit(Event.Error)
            } finally {
                _isSaving.value = false
            }
        }
    }

    sealed interface Event {
        data object Saved : Event
        data object Error : Event
    }
}
