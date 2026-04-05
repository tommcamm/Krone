package com.sofato.krone.domain.usecase.budget

import com.sofato.krone.domain.model.BudgetAllocation
import com.sofato.krone.domain.model.BudgetOverview
import com.sofato.krone.domain.model.CategorySpend
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.IncomeRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Expense
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
    private val getOrCopyForwardAllocations: GetOrCopyForwardAllocationsUseCase,
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
        val month = GetOrCopyForwardAllocationsUseCase.formatMonth(period.startDate)
        emit(Triple(period, currencyCode, LocalDate.today()) to month)
    }.flatMapLatest { (triple, month) ->
        val (period, currencyCode, today) = triple
        val totalsFlow = combine(
            incomeRepository.getTotalRecurringIncomeMinor(),
            recurringExpenseRepository.getTotalActiveRecurringMinor(),
            savingsBucketRepository.getTotalMonthlyContributionsMinor(),
        ) { income, fixed, savings ->
            Triple(income ?: 0L, fixed ?: 0L, savings ?: 0L)
        }
        val dataFlow = combine(
            expenseRepository.getExpensesBetween(period.startDate, today),
            categoryRepository.getActiveCategories(),
            getOrCopyForwardAllocations(month),
        ) { expenses, categories, allocations ->
            Triple(expenses, categories, allocations)
        }
        combine(totalsFlow, dataFlow) { (totalIncome, totalFixed, totalSavings), (expenses, categories, allocations) ->
            val totalSpent = expenses.sumOf { it.homeAmount }
            val discretionary = totalIncome - totalFixed - totalSavings

            val allocationMap = allocations.associate { it.categoryId to it.allocatedAmountMinor }

            val categoryBreakdown = categories.map { cat ->
                val spent = expenses.filter { it.category.id == cat.id }.sumOf { it.homeAmount }
                val allocated = allocationMap[cat.id] ?: 0L
                CategorySpend(category = cat, allocatedMinor = allocated, spentMinor = spent)
            }.filter { it.spentMinor > 0 || it.allocatedMinor > 0 }
                .sortedByDescending { maxOf(it.spentMinor, it.allocatedMinor) }

            val totalAllocated = categoryBreakdown.sumOf { it.allocatedMinor }
            val unallocated = discretionary - totalAllocated

            BudgetOverview(
                period = period,
                totalIncomeMinor = totalIncome,
                totalFixedMinor = totalFixed,
                totalSavingsMinor = totalSavings,
                discretionaryMinor = discretionary,
                spentMinor = totalSpent,
                categoryBreakdown = categoryBreakdown,
                currencyCode = currencyCode,
                totalAllocatedMinor = totalAllocated,
                unallocatedDiscretionaryMinor = unallocated,
            )
        }
    }
}
