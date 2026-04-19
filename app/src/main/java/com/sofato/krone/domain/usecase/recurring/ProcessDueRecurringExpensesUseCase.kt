package com.sofato.krone.domain.usecase.recurring

import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import com.sofato.krone.util.today
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import javax.inject.Inject

class ProcessDueRecurringExpensesUseCase @Inject constructor(
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val currencyRepository: CurrencyRepository,
    private val convertAmountUseCase: ConvertAmountUseCase,
) {
    suspend operator fun invoke() {
        val today = LocalDate.today()
        val dueExpenses = recurringExpenseRepository.getDueRecurring(today)

        for (recurring in dueExpenses) {
            val category = categoryRepository.getCategoryById(recurring.categoryId) ?: continue
            val currency = currencyRepository.getCurrencyByCode(recurring.currencyCode) ?: continue

            // Skip this recurring expense if we can't convert — will retry next run.
            val conversion = convertAmountUseCase(
                amountMinor = recurring.amountMinor,
                fromCode = recurring.currencyCode,
                date = recurring.nextDate,
            ) ?: continue

            val expense = Expense(
                amount = recurring.amountMinor,
                currency = currency,
                homeAmount = conversion.convertedAmountMinor,
                exchangeRateUsed = conversion.rateUsed,
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
