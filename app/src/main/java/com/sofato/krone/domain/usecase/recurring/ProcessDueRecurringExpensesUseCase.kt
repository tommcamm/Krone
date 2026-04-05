package com.sofato.krone.domain.usecase.recurring

import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.math.roundToLong

class ProcessDueRecurringExpensesUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
) {
    suspend operator fun invoke() {
        val today = LocalDate.today()
        val dueExpenses = recurringExpenseRepository.getDueRecurring(today)
        val homeCurrency = userPreferencesRepository.homeCurrencyCode.first()

        for (recurring in dueExpenses) {
            val category = categoryRepository.getCategoryById(recurring.categoryId) ?: continue
            val currency = currencyRepository.getCurrencyByCode(recurring.currencyCode) ?: continue

            val homeAmount: Long
            val rateUsed: Double
            if (recurring.currencyCode == homeCurrency) {
                homeAmount = recurring.amountMinor
                rateUsed = 1.0
            } else {
                val rate = exchangeRateRepository.getRate(recurring.currencyCode, homeCurrency)
                if (rate != null) {
                    rateUsed = rate.rate
                    homeAmount = (recurring.amountMinor * rateUsed).roundToLong()
                } else {
                    // Skip this recurring expense if we can't convert — will retry next run
                    continue
                }
            }

            val expense = Expense(
                amount = recurring.amountMinor,
                currency = currency,
                homeAmount = homeAmount,
                exchangeRateUsed = rateUsed,
                category = category,
                note = recurring.label,
                date = recurring.nextDate,
                createdAt = Clock.System.now(),
                isRecurringInstance = true,
                recurringExpenseId = recurring.id,
            )
            expenseRepository.addExpense(expense)

            // Advance date by configured cadence, respecting charge day.
            val nextDate = RecurrenceRule.advanceNextDate(
                current = recurring.nextDate,
                recurrenceRule = recurring.recurrenceRule,
                dayOfMonth = recurring.dayOfMonth,
            )
            recurringExpenseRepository.updateNextDate(recurring.id, nextDate)
        }
    }
}
