package com.sofato.krone.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Defaults
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import com.sofato.krone.domain.usecase.expense.AddExpenseUseCase
import com.sofato.krone.domain.usecase.expense.DeleteExpenseUseCase
import com.sofato.krone.domain.usecase.expense.GetExpenseByIdUseCase
import com.sofato.krone.domain.usecase.expense.UpdateExpenseUseCase
import com.sofato.krone.util.CalculatorEngine
import com.sofato.krone.util.CalculatorState
import com.sofato.krone.util.CurrencyFormatter
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
class ExpenseSheetViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    convertAmountUseCase: ConvertAmountUseCase,
    exchangeRateRepository: ExchangeRateRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enabledCurrencies: StateFlow<List<Currency>> =
        currencyRepository.getEnabledCurrencies()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val calculatorEngine = CalculatorEngine()

    private val _calculatorState = MutableStateFlow(CalculatorState())
    val calculatorState: StateFlow<CalculatorState> = _calculatorState.asStateFlow()

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

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _editingExpenseId = MutableStateFlow<Long?>(null)

    private val _events = MutableSharedFlow<ExpenseSheetEvent>()
    val events = _events.asSharedFlow()

    // For conversion preview, we feed a synthetic amountInput to ConversionPreviewHelper
    private val _amountForConversion = MutableStateFlow("")
    private val conversionHelper = ConversionPreviewHelper(
        currencyRepository, userPreferencesRepository, convertAmountUseCase, exchangeRateRepository,
    )
    val convertedAmountText: StateFlow<String?> = conversionHelper.convertedAmountText
    val rateFreshness: StateFlow<RateFreshness> = conversionHelper.rateFreshness
    val isForeignCurrency: StateFlow<Boolean> = conversionHelper.isForeignCurrency

    private var isInitialized = false

    private fun ensureInitialized() {
        if (isInitialized) return
        isInitialized = true
        conversionHelper.startObserving(viewModelScope, _amountForConversion, _selectedCurrency)
    }

    fun resetForNew(categoryId: Long? = null) {
        ensureInitialized()
        calculatorEngine.onClear()
        _calculatorState.value = calculatorEngine.state
        _selectedCategory.value = null
        _noteInput.value = ""
        _selectedDate.value = LocalDate.today()
        _isSaving.value = false
        _isEditMode.value = false
        _editingExpenseId.value = null
        _amountForConversion.value = ""

        viewModelScope.launch {
            val homeCode = userPreferencesRepository.homeCurrencyCode.first()
            val currencies = currencyRepository.getEnabledCurrencies().first()
            _selectedCurrency.value = currencies.find { it.code == homeCode } ?: currencies.firstOrNull()

            if (categoryId != null && categoryId > 0) {
                val allCategories = categories.first { it.isNotEmpty() }
                _selectedCategory.value = allCategories.find { it.id == categoryId }
            }
        }
    }

    fun loadForEdit(expenseId: Long) {
        ensureInitialized()
        _isEditMode.value = true
        _editingExpenseId.value = expenseId
        _isSaving.value = false

        viewModelScope.launch {
            val expense = getExpenseByIdUseCase(expenseId) ?: return@launch
            val amountPlain = CurrencyFormatter.formatPlain(expense.amount, expense.currency.decimalPlaces)
            calculatorEngine.setState(amountPlain)
            _calculatorState.value = calculatorEngine.state
            _selectedCategory.value = expense.category
            _selectedCurrency.value = expense.currency
            _noteInput.value = expense.note ?: ""
            _selectedDate.value = expense.date
            _amountForConversion.value = amountPlain
        }
    }

    fun onDigit(digit: Char) {
        calculatorEngine.onDigit(digit)
        syncCalculatorState()
    }

    fun onDecimal() {
        calculatorEngine.onDecimal()
        syncCalculatorState()
    }

    fun onOperator(op: Char) {
        // Map display symbols to internal operators
        val internalOp = when (op) {
            '\u00D7', 'x', 'X' -> '*'
            '\u00F7' -> '/'
            '\u2212' -> '-'
            else -> op
        }
        calculatorEngine.onOperator(internalOp)
        syncCalculatorState()
    }

    fun onBackspace() {
        calculatorEngine.onBackspace()
        syncCalculatorState()
    }

    private fun syncCalculatorState() {
        _calculatorState.value = calculatorEngine.state
        _amountForConversion.value = calculatorEngine.getDisplayAmount()
    }

    fun onCategorySelected(category: Category) {
        _selectedCategory.value = if (_selectedCategory.value?.id == category.id) null else category
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

        // Evaluate expression to get final amount
        val amount = calculatorEngine.evaluate()
        if (amount == null) {
            _calculatorState.value = calculatorEngine.state
            return
        }
        _calculatorState.value = calculatorEngine.state

        val amountMinor = (amount * 10.0.pow(currency.decimalPlaces)).roundToLong()

        viewModelScope.launch {
            _isSaving.value = true
            val category = _selectedCategory.value ?: resolveOtherCategory()
            if (category == null) {
                _isSaving.value = false
                return@launch
            }

            if (_isEditMode.value) {
                val expenseId = _editingExpenseId.value
                if (expenseId == null) { _isSaving.value = false; return@launch }
                val original = getExpenseByIdUseCase(expenseId)
                if (original == null) { _isSaving.value = false; return@launch }
                val updated = original.copy(
                    amount = amountMinor,
                    currency = currency,
                    category = category,
                    note = _noteInput.value.takeIf { it.isNotBlank() },
                    date = _selectedDate.value,
                )
                val success = updateExpenseUseCase(updated)
                _isSaving.value = false
                if (success) {
                    _events.emit(ExpenseSheetEvent.Saved)
                } else {
                    _events.emit(ExpenseSheetEvent.RateUnavailable)
                }
            } else {
                val result = addExpenseUseCase(
                    amountMinor = amountMinor,
                    currency = currency,
                    category = category,
                    note = _noteInput.value.takeIf { it.isNotBlank() },
                    date = _selectedDate.value,
                )
                _isSaving.value = false
                if (result != null) {
                    _events.emit(ExpenseSheetEvent.Saved)
                } else {
                    _events.emit(ExpenseSheetEvent.RateUnavailable)
                }
            }
        }
    }

    fun delete() {
        val expenseId = _editingExpenseId.value ?: return
        viewModelScope.launch {
            deleteExpenseUseCase(expenseId)
            _events.emit(ExpenseSheetEvent.Deleted)
        }
    }

    private suspend fun resolveOtherCategory(): Category? {
        return categories.first { it.isNotEmpty() }
            .find { it.iconName == Defaults.OTHER_CATEGORY_ICON && !it.isCustom }
    }

    sealed interface ExpenseSheetEvent {
        data object Saved : ExpenseSheetEvent
        data object Deleted : ExpenseSheetEvent
        data object RateUnavailable : ExpenseSheetEvent
    }
}
