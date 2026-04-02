package com.sofato.krone.domain.usecase.insights

import com.sofato.krone.domain.model.CategoryMonthlySpend
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.usecase.budget.CalculateBudgetPeriodUseCase
import com.sofato.krone.util.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject

class GetCategoryComparisonUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val calculateBudgetPeriod: CalculateBudgetPeriodUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<CategoryMonthlySpend>> = flow {
        val today = LocalDate.today()
        val currentPeriod = calculateBudgetPeriod(today)
        val previousPeriod = calculateBudgetPeriod(
            currentPeriod.startDate.minus(1, DateTimeUnit.DAY),
        )
        emit(Triple(currentPeriod, previousPeriod, today))
    }.flatMapLatest { (current, previous, today) ->
        combine(
            expenseRepository.getCategoryTotals(current.startDate, today),
            expenseRepository.getCategoryTotals(previous.startDate, previous.endDate),
            categoryRepository.getActiveCategories(),
        ) { currentTotals, previousTotals, categories ->
            val prevMap = previousTotals.associate { it.categoryId to it.totalMinor }
            val currentMap = currentTotals.associate { it.categoryId to it.totalMinor }

            categories
                .map { cat ->
                    CategoryMonthlySpend(
                        category = cat,
                        currentMonthMinor = currentMap[cat.id] ?: 0L,
                        previousMonthMinor = prevMap[cat.id] ?: 0L,
                    )
                }
                .filter { it.currentMonthMinor > 0 || it.previousMonthMinor > 0 }
                .sortedByDescending { it.currentMonthMinor }
        }
    }
}
