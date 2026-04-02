package com.sofato.krone.ui.expenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import com.sofato.krone.domain.usecase.expense.AddExpenseUseCase
import com.sofato.krone.ui.navigation.KroneDestination
import com.sofato.krone.util.CurrencyFormatter
import com.sofato.krone.util.today
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.days

enum class RateFreshness { FRESH, STALE, UNAVAILABLE }

@OptIn(FlowPreview::class)
@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val convertAmountUseCase: ConvertAmountUseCase,
    private val exchangeRateRepository: ExchangeRateRepository,
) : ViewModel() {

    private val initialCategoryId: Long = savedStateHandle.toRoute<KroneDestination.AddExpense>().categoryId

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

    private val _convertedAmountText = MutableStateFlow<String?>(null)
    val convertedAmountText: StateFlow<String?> = _convertedAmountText.asStateFlow()

    private val _rateFreshness = MutableStateFlow(RateFreshness.UNAVAILABLE)
    val rateFreshness: StateFlow<RateFreshness> = _rateFreshness.asStateFlow()

    private val _isForeignCurrency = MutableStateFlow(false)
    val isForeignCurrency: StateFlow<Boolean> = _isForeignCurrency.asStateFlow()

    init {
        viewModelScope.launch {
            val homeCode = userPreferencesRepository.homeCurrencyCode.first()
            val currencies = currencyRepository.getEnabledCurrencies().first()
            _selectedCurrency.value = currencies.find { it.code == homeCode } ?: currencies.firstOrNull()

            if (initialCategoryId > 0) {
                val allCategories = categories.first { it.isNotEmpty() }
                _selectedCategory.value = allCategories.find { it.id == initialCategoryId }
            }
        }

        // Debounced conversion preview
        viewModelScope.launch {
            combine(_amountInput.debounce(300), _selectedCurrency) { amount, currency ->
                amount to currency
            }.collect { (amountText, currency) ->
                updateConversionPreview(amountText, currency)
            }
        }

        // Rate freshness
        viewModelScope.launch {
            _selectedCurrency.collect { currency ->
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

    fun onAmountChanged(value: String) {
        _amountInput.value = value.filter { it.isDigit() || it == '.' || it == ',' }
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
        val amountText = _amountInput.value.replace(",", ".")
        val amountDouble = amountText.toDoubleOrNull() ?: return
        if (amountDouble <= 0) return

        val amountMinor = (amountDouble * 10.0.pow(currency.decimalPlaces)).roundToLong()

        viewModelScope.launch {
            _isSaving.value = true
            val category = _selectedCategory.value ?: resolveOtherCategory()
            if (category == null) {
                _isSaving.value = false
                return@launch
            }
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

    private suspend fun resolveOtherCategory(): Category? {
        return categories.first { it.isNotEmpty() }
            .find { it.iconName == "MoreHoriz" && !it.isCustom }
    }

    sealed interface AddExpenseEvent {
        data object Saved : AddExpenseEvent
    }
}
