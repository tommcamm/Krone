package com.sofato.krone.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.BudgetOverview
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Income
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.budget.GetBudgetOverviewUseCase
import com.sofato.krone.domain.usecase.income.GetRecurringIncomeUseCase
import com.sofato.krone.domain.usecase.recurring.GetActiveRecurringExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    getBudgetOverviewUseCase: GetBudgetOverviewUseCase,
    getActiveRecurringExpensesUseCase: GetActiveRecurringExpensesUseCase,
    getRecurringIncomeUseCase: GetRecurringIncomeUseCase,
    currencyRepository: CurrencyRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val budgetOverview: StateFlow<BudgetOverview?> =
        getBudgetOverviewUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recurringExpenses: StateFlow<List<RecurringExpense>> =
        getActiveRecurringExpensesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val income: StateFlow<List<Income>> =
        getRecurringIncomeUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeCurrency: StateFlow<Currency?> =
        userPreferencesRepository.homeCurrencyCode
            .flatMapLatest { code ->
                currencyRepository.getEnabledCurrencies()
                    .combine(flowOf(code)) { currencies, homeCode ->
                        currencies.find { it.code == homeCode }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
