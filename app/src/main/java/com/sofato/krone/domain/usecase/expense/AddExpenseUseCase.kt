package com.sofato.krone.domain.usecase.expense

import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(
        amountMinor: Long,
        currency: Currency,
        category: Category,
        note: String?,
        date: LocalDate,
    ): Long {
        val homeCurrencyCode = userPreferencesRepository.homeCurrencyCode.first()
        val homeAmount: Long
        val rateUsed: Double
        if (currency.code == homeCurrencyCode) {
            homeAmount = amountMinor
            rateUsed = 1.0
        } else {
            // Phase 1: no exchange rate fetching. Use 1:1 placeholder.
            // Full conversion comes in Phase 3.
            rateUsed = 1.0
            homeAmount = amountMinor
        }

        val expense = Expense(
            amount = amountMinor,
            currency = currency,
            homeAmount = homeAmount,
            exchangeRateUsed = rateUsed,
            category = category,
            note = note?.takeIf { it.isNotBlank() },
            date = date,
            createdAt = Clock.System.now(),
        )
        return expenseRepository.addExpense(expense)
    }
}
