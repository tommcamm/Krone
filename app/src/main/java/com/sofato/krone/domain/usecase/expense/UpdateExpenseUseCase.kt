package com.sofato.krone.domain.usecase.expense

import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import javax.inject.Inject

class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val convertAmountUseCase: ConvertAmountUseCase,
) {
    /**
     * Returns true if the update succeeded, false if the exchange rate was unavailable
     * for a foreign-currency expense.
     */
    suspend operator fun invoke(expense: Expense): Boolean {
        val conversion = convertAmountUseCase(expense.amount, expense.currency.code, expense.date)
            ?: return false
        val converted = expense.copy(
            homeAmount = conversion.convertedAmountMinor,
            exchangeRateUsed = conversion.rateUsed,
        )
        expenseRepository.updateExpense(converted)
        return true
    }
}
