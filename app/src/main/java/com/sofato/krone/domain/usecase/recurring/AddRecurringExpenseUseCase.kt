package com.sofato.krone.domain.usecase.recurring

import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import javax.inject.Inject

class AddRecurringExpenseUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
) {
    suspend operator fun invoke(expense: RecurringExpense): Long =
        recurringExpenseRepository.addRecurringExpense(expense)
}
