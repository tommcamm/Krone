package com.sofato.krone.domain.usecase.recurring

import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveRecurringExpensesUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
) {
    operator fun invoke(): Flow<List<RecurringExpense>> =
        recurringExpenseRepository.getActiveRecurring()
}
