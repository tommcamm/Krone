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
    suspend operator fun invoke(expense: Expense) {
        val homeCurrencyCode = userPreferencesRepository.homeCurrencyCode.first()
        val converted = if (expense.currency.code == homeCurrencyCode) {
            expense.copy(homeAmount = expense.amount, exchangeRateUsed = 1.0)
        } else {
            val rate = exchangeRateRepository.getRate(expense.currency.code, homeCurrencyCode)
            if (rate != null) {
                expense.copy(
                    homeAmount = (expense.amount * rate.rate).roundToLong(),
                    exchangeRateUsed = rate.rate,
                )
            } else {
                expense.copy(homeAmount = 0, exchangeRateUsed = 0.0)
            }
        }
        expenseRepository.updateExpense(converted)
    }
}
