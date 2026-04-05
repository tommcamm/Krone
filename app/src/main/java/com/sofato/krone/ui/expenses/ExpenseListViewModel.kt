package com.sofato.krone.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.expense.DeleteExpenseUseCase
import com.sofato.krone.domain.usecase.expense.GetRecentExpensesUseCase
import com.sofato.krone.domain.usecase.expense.RestoreExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    getRecentExpensesUseCase: GetRecentExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val restoreExpenseUseCase: RestoreExpenseUseCase,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val groupedExpenses: StateFlow<Map<LocalDate, List<Expense>>> =
        getRecentExpensesUseCase(100)
            .map { expenses ->
                expenses.groupBy { it.date }
                    .toSortedMap(compareByDescending { it })
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _homeCurrency = MutableStateFlow<Currency?>(null)
    val homeCurrency: StateFlow<Currency?> = _homeCurrency.asStateFlow()

    init {
        viewModelScope.launch {
            val code = userPreferencesRepository.homeCurrencyCode.first()
            _homeCurrency.value = currencyRepository.getCurrencyByCode(code)
        }
    }

    private val _lastDeletedExpense = MutableStateFlow<Expense?>(null)
    val lastDeletedExpense: StateFlow<Expense?> = _lastDeletedExpense.asStateFlow()

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                _lastDeletedExpense.value = expense
                deleteExpenseUseCase(expense.id)
            } catch (_: Exception) { /* best-effort */ }
        }
    }

    fun undoDelete() {
        val expense = _lastDeletedExpense.value ?: return
        _lastDeletedExpense.value = null
        viewModelScope.launch {
            try {
                restoreExpenseUseCase(expense)
            } catch (_: Exception) { /* best-effort */ }
        }
    }

    fun clearDeletedExpense() {
        _lastDeletedExpense.value = null
    }
}
