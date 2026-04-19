package com.sofato.krone.domain.usecase.expense

import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    operator fun invoke(): Flow<List<Expense>> = expenseRepository.getAllExpenses()
}
