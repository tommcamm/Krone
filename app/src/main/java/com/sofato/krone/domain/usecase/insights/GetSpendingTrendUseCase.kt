package com.sofato.krone.domain.usecase.insights

import com.sofato.krone.domain.model.MonthlySnapshot
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.MonthlySnapshotRepository
import com.sofato.krone.domain.usecase.budget.CalculateBudgetPeriodUseCase
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import javax.inject.Inject

class GetSpendingTrendUseCase @Inject constructor(
    private val monthlySnapshotRepository: MonthlySnapshotRepository,
    private val expenseRepository: ExpenseRepository,
    private val calculateBudgetPeriod: CalculateBudgetPeriodUseCase,
) {
    data class MonthlyTrend(
        val month: String,
        val totalSpendingMinor: Long,
        val totalSavingsMinor: Long,
        val totalIncomeMinor: Long,
    )

    operator fun invoke(): Flow<List<MonthlyTrend>> {
        val today = LocalDate.today()
        val currentMonth = "${today.year}-${today.month.number.toString().padStart(2, '0')}"

        return combine(
            monthlySnapshotRepository.getAllSnapshots(),
            expenseRepository.getTotalHomeAmountBetween(
                LocalDate(today.year, today.month, 1),
                today,
            ),
        ) { snapshots, currentMonthSpent ->
            val trend = snapshots
                .filter { it.month < currentMonth }
                .sortedBy { it.month }
                .takeLast(11)
                .map { snapshot ->
                    MonthlyTrend(
                        month = snapshot.month,
                        totalSpendingMinor = snapshot.totalVariableMinor + snapshot.totalFixedMinor,
                        totalSavingsMinor = snapshot.totalSavingsMinor,
                        totalIncomeMinor = snapshot.totalIncomeMinor,
                    )
                }
                .toMutableList()

            // Add current month with live data
            trend += MonthlyTrend(
                month = currentMonth,
                totalSpendingMinor = currentMonthSpent ?: 0L,
                totalSavingsMinor = 0L,
                totalIncomeMinor = 0L,
            )

            trend
        }
    }
}
