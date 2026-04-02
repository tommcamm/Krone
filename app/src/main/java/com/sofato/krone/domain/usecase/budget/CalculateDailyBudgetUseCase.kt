package com.sofato.krone.domain.usecase.budget

import com.sofato.krone.domain.model.DailyBudget
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.IncomeRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class CalculateDailyBudgetUseCase @Inject constructor(
    private val calculateBudgetPeriod: CalculateBudgetPeriodUseCase,
    private val incomeRepository: IncomeRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val savingsBucketRepository: SavingsBucketRepository,
    private val expenseRepository: ExpenseRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<DailyBudget> = flow {
        val period = calculateBudgetPeriod()
        val today = LocalDate.today()
        val currencyCode = userPreferencesRepository.homeCurrencyCode.first()
        emit(Triple(period, today, currencyCode))
    }.flatMapLatest { (period, today, currencyCode) ->
        combine(
            incomeRepository.getTotalRecurringIncomeMinor(),
            recurringExpenseRepository.getTotalActiveRecurringMinor(),
            savingsBucketRepository.getTotalMonthlyContributionsMinor(),
            expenseRepository.getTotalHomeAmountBetween(period.startDate, today),
        ) { income, fixed, savings, spent ->
            val totalIncome = income ?: 0L
            val totalFixed = fixed ?: 0L
            val totalSavings = savings ?: 0L
            val spentSoFar = spent ?: 0L
            val discretionary = totalIncome - totalFixed - totalSavings
            val remaining = discretionary - spentSoFar
            val remainingDays = period.remainingDaysFrom(today)
            val daily = if (remainingDays > 0) remaining / remainingDays else 0L

            DailyBudget(
                dailyAmountMinor = daily,
                totalIncomeMinor = totalIncome,
                totalFixedMinor = totalFixed,
                totalSavingsMinor = totalSavings,
                spentSoFarMinor = spentSoFar,
                remainingDays = remainingDays,
                discretionaryMinor = discretionary,
                currencyCode = currencyCode,
            )
        }
    }
}
