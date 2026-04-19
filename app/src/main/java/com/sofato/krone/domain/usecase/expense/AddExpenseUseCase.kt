package com.sofato.krone.domain.usecase.expense

import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import kotlin.math.roundToLong

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
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
        val homeCurrencyCode = userPreferencesRepository.homeCurrencyCode.first()
        val homeAmount: Long
        val rateUsed: Double
        if (currency.code == homeCurrencyCode) {
            homeAmount = amountMinor
            rateUsed = 1.0
        } else {
            val rate = exchangeRateRepository.getRateForDate(currency.code, homeCurrencyCode, date)
                ?: return null
            rateUsed = rate.rate
            homeAmount = (amountMinor * rateUsed).roundToLong()
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
