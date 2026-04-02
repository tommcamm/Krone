package com.sofato.krone.domain.usecase.expense

import com.sofato.krone.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(id: Long) {
        expenseRepository.deleteExpense(id)
    }
}
