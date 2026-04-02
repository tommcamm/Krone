package com.sofato.krone.domain.usecase.insights

import com.sofato.krone.domain.model.SpendingStreak
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.usecase.budget.CalculateDailyBudgetUseCase
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import javax.inject.Inject

class GetSpendingStreakUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val calculateDailyBudget: CalculateDailyBudgetUseCase,
) {
    operator fun invoke(): Flow<SpendingStreak> {
        val today = LocalDate.today()
        val lookbackStart = today.minus(90, DateTimeUnit.DAY)
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        return combine(
            expenseRepository.getDailyTotals(lookbackStart, yesterday),
            calculateDailyBudget(),
        ) { dailyTotals, dailyBudget ->
            val budgetLimit = dailyBudget.dailyAmountMinor
            if (budgetLimit <= 0) return@combine SpendingStreak(0, 0)

            val spendByDate = dailyTotals.associate { it.date to it.totalMinor }

            // Walk backward from yesterday for current streak
            var currentStreak = 0
            var date = yesterday
            while (date >= lookbackStart) {
                val spent = spendByDate[date] ?: 0L
                if (spent <= budgetLimit) {
                    currentStreak++
                } else {
                    break
                }
                date = date.minus(1, DateTimeUnit.DAY)
            }

            // Find longest streak in the range
            var longestStreak = 0
            var runningStreak = 0
            var scanDate = lookbackStart
            while (scanDate <= yesterday) {
                val spent = spendByDate[scanDate] ?: 0L
                if (spent <= budgetLimit) {
                    runningStreak++
                    if (runningStreak > longestStreak) longestStreak = runningStreak
                } else {
                    runningStreak = 0
                }
                scanDate = scanDate.plus(1, DateTimeUnit.DAY)
            }

            SpendingStreak(currentDays = currentStreak, longestDays = longestStreak)
        }
    }
}
