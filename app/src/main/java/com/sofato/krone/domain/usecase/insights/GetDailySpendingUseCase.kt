package com.sofato.krone.domain.usecase.insights

import com.sofato.krone.domain.model.DailySpend
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.usecase.budget.CalculateBudgetPeriodUseCase
import com.sofato.krone.util.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetDailySpendingUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val calculateBudgetPeriod: CalculateBudgetPeriodUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<List<DailySpend>> = flow {
        val period = calculateBudgetPeriod()
        emit(Pair(startDate ?: period.startDate, endDate ?: LocalDate.today()))
    }.flatMapLatest { (start, end) ->
        expenseRepository.getDailyTotals(start, end).map { totals ->
            totals.map { DailySpend(date = it.date, totalMinor = it.totalMinor) }
        }
    }
}
