package com.sofato.krone.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.expense.AddExpenseUseCase
import com.sofato.krone.util.today
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
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enabledCurrencies: StateFlow<List<Currency>> =
        currencyRepository.getEnabledCurrencies()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedCurrency = MutableStateFlow<Currency?>(null)
    val selectedCurrency: StateFlow<Currency?> = _selectedCurrency.asStateFlow()

    private val _noteInput = MutableStateFlow("")
    val noteInput: StateFlow<String> = _noteInput.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _events = MutableSharedFlow<AddExpenseEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val homeCode = userPreferencesRepository.homeCurrencyCode.first()
            val currencies = currencyRepository.getEnabledCurrencies().first()
            _selectedCurrency.value = currencies.find { it.code == homeCode } ?: currencies.firstOrNull()
        }
    }

    fun onAmountChanged(value: String) {
        _amountInput.value = value.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun onCategorySelected(category: Category) {
        _selectedCategory.value = category
    }

    fun onCurrencySelected(currency: Currency) {
        _selectedCurrency.value = currency
    }

    fun onNoteChanged(value: String) {
        _noteInput.value = value
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun save() {
        val currency = _selectedCurrency.value ?: return
        val category = _selectedCategory.value ?: return
        val amountText = _amountInput.value.replace(",", ".")
        val amountDouble = amountText.toDoubleOrNull() ?: return
        if (amountDouble <= 0) return

        val amountMinor = (amountDouble * 10.0.pow(currency.decimalPlaces)).roundToLong()

        viewModelScope.launch {
            _isSaving.value = true
            addExpenseUseCase(
                amountMinor = amountMinor,
                currency = currency,
                category = category,
                note = _noteInput.value,
                date = _selectedDate.value,
            )
            _isSaving.value = false
            _events.emit(AddExpenseEvent.Saved)
        }
    }

    sealed interface AddExpenseEvent {
        data object Saved : AddExpenseEvent
    }
}
