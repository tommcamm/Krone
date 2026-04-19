package com.sofato.krone.domain.usecase.expense

import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val convertAmountUseCase: ConvertAmountUseCase,
) {
    /**
     * Returns the ID of the created expense, or null if the exchange rate was unavailable
     * for a foreign-currency expense.
     */
    suspend operator fun invoke(
        amountMinor: Long,
        currency: Currency,
        category: Category,
        note: String?,
        date: LocalDate,
    ): Long? {
        val conversion = convertAmountUseCase(amountMinor, currency.code, date) ?: return null

        val expense = Expense(
            amount = amountMinor,
            currency = currency,
            homeAmount = conversion.convertedAmountMinor,
            exchangeRateUsed = conversion.rateUsed,
            category = category,
            note = note?.takeIf { it.isNotBlank() },
            date = date,
            createdAt = Clock.System.now(),
        )
        return expenseRepository.addExpense(expense)
    }
}
