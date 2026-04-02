package com.sofato.krone.domain.usecase.expense

import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetExpensesBetweenDatesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> =
        expenseRepository.getExpensesBetween(startDate, endDate)
}
