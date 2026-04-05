package com.sofato.krone.domain.usecase.recurring

import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.usecase.budget.CalculateBudgetPeriodUseCase
import com.sofato.krone.util.today
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class DeactivateRecurringExpenseUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val expenseRepository: ExpenseRepository,
    private val calculateBudgetPeriod: CalculateBudgetPeriodUseCase,
) {
    suspend operator fun invoke(id: Long) {
        val period = calculateBudgetPeriod()
        expenseRepository.deleteRecurringInstances(id, period.startDate, LocalDate.today())
        recurringExpenseRepository.deactivate(id)
    }
}
