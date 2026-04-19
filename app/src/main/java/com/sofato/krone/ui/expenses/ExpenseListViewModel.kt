package com.sofato.krone.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.expense.DeleteExpenseUseCase
import com.sofato.krone.domain.usecase.expense.GetAllExpensesUseCase
import com.sofato.krone.domain.usecase.expense.RestoreExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    getAllExpensesUseCase: GetAllExpensesUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val restoreExpenseUseCase: RestoreExpenseUseCase,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(ExpenseFilter.Empty)
    val filter: StateFlow<ExpenseFilter> = _filter.asStateFlow()

    private val _sort = MutableStateFlow(ExpenseSort.DateNewest)
    val sort: StateFlow<ExpenseSort> = _sort.asStateFlow()

    val expenses: StateFlow<List<Expense>> =
        combine(getAllExpensesUseCase(), _filter, _sort) { all, f, s ->
            val resolvedRange = f.dateRange.resolve()
            val filtered = all.asSequence()
                .filter { expense ->
                    resolvedRange == null ||
                        (expense.date >= resolvedRange.first && expense.date <= resolvedRange.second)
                }
                .filter { f.categoryIds.isEmpty() || it.category.id in f.categoryIds }
                .filter {
                    f.nameQuery.isBlank() ||
                        (it.note?.contains(f.nameQuery, ignoreCase = true) == true)
                }
                .filter { expense ->
                    (f.minAmountMinor == null || expense.homeAmount >= f.minAmountMinor) &&
                        (f.maxAmountMinor == null || expense.homeAmount <= f.maxAmountMinor)
                }
            when (s) {
                ExpenseSort.DateNewest -> filtered.sortedWith(
                    compareByDescending<Expense> { it.date }.thenByDescending { it.createdAt },
                )
                ExpenseSort.DateOldest -> filtered.sortedWith(
                    compareBy<Expense> { it.date }.thenBy { it.createdAt },
                )
                ExpenseSort.AmountHigh -> filtered.sortedByDescending { it.homeAmount }
                ExpenseSort.AmountLow -> filtered.sortedBy { it.homeAmount }
            }.toList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun updateFilter(filter: ExpenseFilter) {
        _filter.value = filter
    }

    fun updateSort(sort: ExpenseSort) {
        _sort.value = sort
    }

    fun clearFilters() {
        _filter.value = ExpenseFilter.Empty
    }

    fun dismissDateRange() {
        _filter.value = _filter.value.copy(dateRange = com.sofato.krone.ui.expenses.DateRange.AllTime)
    }

    fun dismissCategory(categoryId: Long) {
        _filter.value = _filter.value.copy(categoryIds = _filter.value.categoryIds - categoryId)
    }

    fun dismissNameQuery() {
        _filter.value = _filter.value.copy(nameQuery = "")
    }

    fun dismissAmountRange() {
        _filter.value = _filter.value.copy(minAmountMinor = null, maxAmountMinor = null)
    }

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
