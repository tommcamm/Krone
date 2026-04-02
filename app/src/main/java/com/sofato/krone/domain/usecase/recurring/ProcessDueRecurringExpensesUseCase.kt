package com.sofato.krone.domain.usecase.recurring

import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import javax.inject.Inject

class ProcessDueRecurringExpensesUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val currencyRepository: CurrencyRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke() {
        val today = LocalDate.today()
        val dueExpenses = recurringExpenseRepository.getDueRecurring(today)
        val homeCurrency = userPreferencesRepository.homeCurrencyCode.first()

        for (recurring in dueExpenses) {
            val category = categoryRepository.getCategoryById(recurring.categoryId) ?: continue
            val currency = currencyRepository.getCurrencyByCode(recurring.currencyCode) ?: continue

            val homeAmount = if (recurring.currencyCode == homeCurrency) {
                recurring.amountMinor
            } else {
                recurring.amountMinor // Phase 2: no conversion yet
            }

            val expense = Expense(
                amount = recurring.amountMinor,
                currency = currency,
                homeAmount = homeAmount,
                exchangeRateUsed = 1.0,
                category = category,
                note = recurring.label,
                date = recurring.nextDate,
                createdAt = Clock.System.now(),
                isRecurringInstance = true,
                recurringExpenseId = recurring.id,
            )
            expenseRepository.addExpense(expense)

            // Advance date by configured cadence.
            val nextDate = RecurrenceRule.advanceNextDate(
                current = recurring.nextDate,
                recurrenceRule = recurring.recurrenceRule,
            )
            recurringExpenseRepository.updateNextDate(recurring.id, nextDate)
        }
    }
}
