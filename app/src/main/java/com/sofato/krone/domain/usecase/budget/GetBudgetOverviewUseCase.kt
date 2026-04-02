package com.sofato.krone.domain.usecase.budget

import com.sofato.krone.domain.model.BudgetOverview
import com.sofato.krone.domain.model.CategorySpend
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.IncomeRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.util.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetBudgetOverviewUseCase @Inject constructor(
    private val calculateBudgetPeriod: CalculateBudgetPeriodUseCase,
    private val incomeRepository: IncomeRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val savingsBucketRepository: SavingsBucketRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<BudgetOverview> = flow {
        val period = calculateBudgetPeriod()
        val currencyCode = userPreferencesRepository.homeCurrencyCode.first()
        emit(Triple(period, currencyCode, LocalDate.today()))
    }.flatMapLatest { (period, currencyCode, today) ->
        combine(
            incomeRepository.getTotalRecurringIncomeMinor(),
            recurringExpenseRepository.getTotalActiveRecurringMinor(),
            savingsBucketRepository.getTotalMonthlyContributionsMinor(),
            expenseRepository.getExpensesBetween(period.startDate, today),
            categoryRepository.getActiveCategories(),
        ) { income, fixed, savings, expenses, categories ->
            val totalIncome = income ?: 0L
            val totalFixed = fixed ?: 0L
            val totalSavings = savings ?: 0L
            val totalSpent = expenses.sumOf { it.homeAmount }
            val discretionary = totalIncome - totalFixed - totalSavings

            val categoryBreakdown = categories.map { cat ->
                val spent = expenses.filter { it.category.id == cat.id }.sumOf { it.homeAmount }
                CategorySpend(category = cat, allocatedMinor = 0L, spentMinor = spent)
            }.filter { it.spentMinor > 0 }.sortedByDescending { it.spentMinor }

            BudgetOverview(
                period = period,
                totalIncomeMinor = totalIncome,
                totalFixedMinor = totalFixed,
                totalSavingsMinor = totalSavings,
                discretionaryMinor = discretionary,
                spentMinor = totalSpent,
                categoryBreakdown = categoryBreakdown,
                currencyCode = currencyCode,
            )
        }
    }
}
