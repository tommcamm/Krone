package com.sofato.krone.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.recurring.AddRecurringExpenseUseCase
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong

@HiltViewModel
class AddRecurringExpenseViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addRecurringExpenseUseCase: AddRecurringExpenseUseCase,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _recurrenceRule = MutableStateFlow(RecurrenceRule.MONTHLY)
    val recurrenceRule: StateFlow<String> = _recurrenceRule.asStateFlow()

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
    fun onAmountChanged(value: String) {
        _amountInput.value = value.filter { it.isDigit() || it == '.' || it == ',' }
    }
    fun onCategorySelected(category: Category) { _selectedCategory.value = category }
    fun onRecurrenceRuleChanged(value: String) {
        _recurrenceRule.value = RecurrenceRule.normalize(value)
    }

    fun save() {
        val category = _selectedCategory.value ?: return
        val amountText = _amountInput.value.replace(",", ".")
        val amountDouble = amountText.toDoubleOrNull() ?: return
        if (amountDouble <= 0) return
        if (_label.value.isBlank()) return

        val amountMinor = (amountDouble * 10.0.pow(decimalPlaces)).roundToLong()

        viewModelScope.launch {
            _isSaving.value = true
            addRecurringExpenseUseCase(
                RecurringExpense(
                    amountMinor = amountMinor,
                    currencyCode = currencyCode,
                    categoryId = category.id,
                    label = _label.value.trim(),
                    recurrenceRule = _recurrenceRule.value,
                    nextDate = LocalDate.today(),
                    isActive = true,
                    createdAt = Clock.System.now(),
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
