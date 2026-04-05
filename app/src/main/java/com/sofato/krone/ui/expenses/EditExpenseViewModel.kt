package com.sofato.krone.ui.expenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Defaults
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import com.sofato.krone.domain.usecase.expense.DeleteExpenseUseCase
import com.sofato.krone.domain.usecase.expense.GetExpenseByIdUseCase
import com.sofato.krone.domain.usecase.expense.UpdateExpenseUseCase
import com.sofato.krone.ui.navigation.KroneDestination
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
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong

@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    convertAmountUseCase: ConvertAmountUseCase,
    exchangeRateRepository: ExchangeRateRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<KroneDestination.EditExpense>()

    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enabledCurrencies: StateFlow<List<Currency>> =
        currencyRepository.getEnabledCurrencies()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _expense = MutableStateFlow<Expense?>(null)
    val expense: StateFlow<Expense?> = _expense.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _selectedCurrency = MutableStateFlow<Currency?>(null)
    val selectedCurrency: StateFlow<Currency?> = _selectedCurrency.asStateFlow()

    private val _noteInput = MutableStateFlow("")
    val noteInput: StateFlow<String> = _noteInput.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _events = MutableSharedFlow<EditExpenseEvent>()
    val events = _events.asSharedFlow()

    private val conversionHelper = ConversionPreviewHelper(
        currencyRepository, userPreferencesRepository, convertAmountUseCase, exchangeRateRepository,
    )
    val convertedAmountText: StateFlow<String?> = conversionHelper.convertedAmountText
    val rateFreshness: StateFlow<RateFreshness> = conversionHelper.rateFreshness
    val isForeignCurrency: StateFlow<Boolean> = conversionHelper.isForeignCurrency

    init {
        viewModelScope.launch {
            val loaded = getExpenseByIdUseCase(route.expenseId) ?: return@launch
            _expense.value = loaded
            _amountInput.value = CurrencyFormatter.formatPlain(loaded.amount, loaded.currency.decimalPlaces)
            _selectedCategory.value = loaded.category
            _selectedCurrency.value = loaded.currency
            _noteInput.value = loaded.note ?: ""
            _selectedDate.value = loaded.date
        }

        conversionHelper.startObserving(viewModelScope, _amountInput, _selectedCurrency)
    }

    fun onAmountChanged(value: String) {
        _amountInput.value = value.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun onCategorySelected(category: Category) {
        _selectedCategory.value = if (_selectedCategory.value?.id == category.id) null else category
    }
    fun onCurrencySelected(currency: Currency) { _selectedCurrency.value = currency }
    fun onNoteChanged(value: String) { _noteInput.value = value }
    fun onDateSelected(date: LocalDate) { _selectedDate.value = date }

    fun save() {
        val original = _expense.value ?: return
        val currency = _selectedCurrency.value ?: return
        val amountText = _amountInput.value.replace(",", ".")
        val amountDouble = amountText.toDoubleOrNull() ?: return
        if (amountDouble <= 0) return

        val amountMinor = (amountDouble * 10.0.pow(currency.decimalPlaces)).roundToLong()

        viewModelScope.launch {
            val category = _selectedCategory.value ?: resolveOtherCategory() ?: return@launch
            val updated = original.copy(
                amount = amountMinor,
                currency = currency,
                category = category,
                note = _noteInput.value.takeIf { it.isNotBlank() },
                date = _selectedDate.value ?: original.date,
            )
            val success = updateExpenseUseCase(updated)
            if (success) {
                _events.emit(EditExpenseEvent.Saved)
            } else {
                _events.emit(EditExpenseEvent.RateUnavailable)
            }
        }
    }

    private suspend fun resolveOtherCategory(): Category? {
        return categories.first { it.isNotEmpty() }
            .find { it.iconName == Defaults.OTHER_CATEGORY_ICON && !it.isCustom }
    }

    fun delete() {
        viewModelScope.launch {
            _expense.value?.let {
                deleteExpenseUseCase(it.id)
                _events.emit(EditExpenseEvent.Deleted)
            }
        }
    }

    sealed interface EditExpenseEvent {
        data object Saved : EditExpenseEvent
        data object Deleted : EditExpenseEvent
        data object RateUnavailable : EditExpenseEvent
    }
}
