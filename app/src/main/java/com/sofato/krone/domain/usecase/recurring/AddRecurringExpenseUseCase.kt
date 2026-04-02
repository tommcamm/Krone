package com.sofato.krone.domain.usecase.recurring

import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import javax.inject.Inject

class AddRecurringExpenseUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
) {
    suspend operator fun invoke(expense: RecurringExpense): Long {
        val normalized = expense.copy(
            recurrenceRule = RecurrenceRule.normalize(expense.recurrenceRule),
        )
        return recurringExpenseRepository.addRecurringExpense(normalized)
    }
}
