package com.sofato.krone.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.BudgetOverview
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.DailyBudget
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.budget.CalculateBudgetPeriodUseCase
import com.sofato.krone.domain.usecase.budget.CalculateDailyBudgetUseCase
import com.sofato.krone.domain.usecase.budget.GetBudgetOverviewUseCase
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.expense.DeleteExpenseUseCase
import com.sofato.krone.domain.usecase.expense.GetExpensesByDateUseCase
import com.sofato.krone.domain.usecase.expense.GetRecentExpensesUseCase
import com.sofato.krone.domain.usecase.expense.RestoreExpenseUseCase
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.usecase.recurring.ProcessDueRecurringExpensesUseCase
import com.sofato.krone.domain.usecase.savings.ProcessSavingsContributionsUseCase
import com.sofato.krone.util.today
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    getExpensesByDate: GetExpensesByDateUseCase,
    getRecentExpensesUseCase: GetRecentExpensesUseCase,
    private val calculateBudgetPeriodUseCase: CalculateBudgetPeriodUseCase,
    private val processRecurringUseCase: ProcessDueRecurringExpensesUseCase,
    private val processSavingsUseCase: ProcessSavingsContributionsUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val restoreExpenseUseCase: RestoreExpenseUseCase,
    calculateDailyBudgetUseCase: CalculateDailyBudgetUseCase,
    getBudgetOverviewUseCase: GetBudgetOverviewUseCase,
    getCategoriesUseCase: GetCategoriesUseCase,
    userPreferencesRepository: UserPreferencesRepository,
    currencyRepository: CurrencyRepository,
) : ViewModel() {

    private val today = LocalDate.today()

    val homeCurrency: StateFlow<Currency?> =
        userPreferencesRepository.homeCurrencyCode
            .flatMapLatest { code ->
                currencyRepository.getEnabledCurrencies()
                    .combine(flowOf(code)) { currencies, homeCode ->
                        currencies.find { it.code == homeCode }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalSpentToday: StateFlow<Long> =
        getExpensesByDate(today)
            .map { expenses -> expenses.filter { !it.isRecurringInstance }.sumOf { it.homeAmount } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val recentExpenses: StateFlow<List<Expense>> =
        getRecentExpensesUseCase(RECENT_EXPENSES_PREVIEW_LIMIT)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyBudget: StateFlow<DailyBudget?> =
        calculateDailyBudgetUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rollingDailyAverage: StateFlow<Long> =
        combine(dailyBudget, totalSpentToday) { budget, spentToday ->
            if (budget == null || budget.remainingDays <= 0) return@combine 0L
            val totalSpent = budget.spentSoFarMinor + spentToday
            val period = calculateBudgetPeriodUseCase()
            val elapsedDays = period.totalDays - budget.remainingDays + 1
            if (elapsedDays > 0) totalSpent / elapsedDays else 0L
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val budgetOverview: StateFlow<BudgetOverview?> =
        getBudgetOverviewUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Positive = projected overspend, negative = projected underspend */
    val projectedEndOfMonth: StateFlow<Long> =
        combine(rollingDailyAverage, dailyBudget, totalSpentToday) { avg, budget, spentToday ->
            if (budget == null) return@combine 0L
            val totalSpentSoFar = budget.spentSoFarMinor + spentToday
            val projected = totalSpentSoFar + avg * budget.remainingDays
            projected - budget.discretionaryMinor
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _lastDeletedExpense = MutableStateFlow<Expense?>(null)
    val lastDeletedExpense: StateFlow<Expense?> = _lastDeletedExpense.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                processRecurringUseCase()
                processSavingsUseCase()
            } catch (_: Exception) {
                // Non-critical; don't crash the dashboard
            }
        }
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

    private companion object {
        const val RECENT_EXPENSES_PREVIEW_LIMIT = 5
    }
}
