package com.sofato.krone.ui.recurring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.ui.navigation.KroneDestination
import com.sofato.krone.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToLong

@HiltViewModel
class EditRecurringExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCategoriesUseCase: GetCategoriesUseCase,
    private val recurringExpenseRepository: RecurringExpenseRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<KroneDestination.EditRecurringExpense>()

    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _expense = MutableStateFlow<RecurringExpense?>(null)

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _recurrenceRule = MutableStateFlow(RecurrenceRule.MONTHLY)
    val recurrenceRule: StateFlow<String> = _recurrenceRule.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val loaded = recurringExpenseRepository.getById(route.expenseId) ?: return@launch
            _expense.value = loaded
            _label.value = loaded.label
            _amountInput.value = CurrencyFormatter.formatPlain(loaded.amountMinor, 2)
            _recurrenceRule.value = RecurrenceRule.normalize(loaded.recurrenceRule)
            // Find matching category once categories load
            val cats = categories.value
            _selectedCategory.value = cats.find { it.id == loaded.categoryId }
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
        val original = _expense.value ?: return
        val category = _selectedCategory.value ?: return
        val amountText = _amountInput.value.replace(",", ".")
        val amountDouble = amountText.toDoubleOrNull() ?: return
        if (amountDouble <= 0 || _label.value.isBlank()) return

        val amountMinor = (amountDouble * 10.0.pow(2)).roundToLong()

        viewModelScope.launch {
            recurringExpenseRepository.updateRecurringExpense(
                original.copy(
                    label = _label.value.trim(),
                    amountMinor = amountMinor,
                    categoryId = category.id,
                    recurrenceRule = _recurrenceRule.value,
                )
            )
            _events.emit(Event.Saved)
        }
    }

    fun deactivate() {
        viewModelScope.launch {
            _expense.value?.let {
                recurringExpenseRepository.deactivate(it.id)
                _events.emit(Event.Deactivated)
            }
        }
    }

    sealed interface Event {
        data object Saved : Event
        data object Deactivated : Event
    }
}
