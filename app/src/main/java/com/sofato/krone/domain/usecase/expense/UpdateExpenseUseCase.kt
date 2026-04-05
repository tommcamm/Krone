package com.sofato.krone.domain.usecase.expense

import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.roundToLong

class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
) {
    /**
     * Returns true if the update succeeded, false if the exchange rate was unavailable
     * for a foreign-currency expense.
     */
    suspend operator fun invoke(expense: Expense): Boolean {
        val homeCurrencyCode = userPreferencesRepository.homeCurrencyCode.first()
        val converted = if (expense.currency.code == homeCurrencyCode) {
            expense.copy(homeAmount = expense.amount, exchangeRateUsed = 1.0)
        } else {
            val rate = exchangeRateRepository.getRate(expense.currency.code, homeCurrencyCode)
                ?: return false
            expense.copy(
                homeAmount = (expense.amount * rate.rate).roundToLong(),
                exchangeRateUsed = rate.rate,
            )
        }
        expenseRepository.updateExpense(converted)
        return true
    }
}
