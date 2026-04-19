package com.sofato.krone.domain.usecase.recurring

import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.ExchangeRate
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

class ProcessDueRecurringExpensesUseCaseTest {

    private val dkk = Currency("DKK", "Danish Krone", "kr.", 2, SymbolPosition.AFTER, true, 0)
    private val eur = Currency("EUR", "Euro", "€", 2, SymbolPosition.BEFORE, true, 1)
    private val food = Category(10, "Food", "restaurant", "#FF0000", false, 0)

    private lateinit var recurringExpenseRepository: RecurringExpenseRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var exchangeRateRepository: ExchangeRateRepository
    private lateinit var useCase: ProcessDueRecurringExpensesUseCase

    @Before
    fun setUp() {
        recurringExpenseRepository = mockk(relaxed = true)
        expenseRepository = mockk(relaxed = true)
        categoryRepository = mockk {
            coEvery { getCategoryById(10) } returns food
        }
        currencyRepository = mockk {
            coEvery { getCurrencyByCode("EUR") } returns eur
            coEvery { getCurrencyByCode("DKK") } returns dkk
        }
        userPreferencesRepository = mockk {
            every { homeCurrencyCode } returns flowOf("DKK")
        }
        exchangeRateRepository = mockk()
        coEvery { expenseRepository.addExpense(any()) } returns 1L

        useCase = ProcessDueRecurringExpensesUseCase(
            recurringExpenseRepository,
            expenseRepository,
            categoryRepository,
            currencyRepository,
            userPreferencesRepository,
            exchangeRateRepository,
        )
    }

    private fun recurring(id: Long, nextDate: LocalDate, code: String = "EUR", amount: Long = 10000) = RecurringExpense(
        id = id,
        amountMinor = amount,
        currencyCode = code,
        categoryId = 10,
        label = "Rent",
        recurrenceRule = RecurrenceRule.MONTHLY,
        nextDate = nextDate,
        isActive = true,
        createdAt = Instant.fromEpochSeconds(0),
    )

    @Test
    fun `rate is locked against the occurrence date, not today`() = runTest {
        val occurrenceDate = LocalDate(2026, 1, 1)
        coEvery { recurringExpenseRepository.getDueRecurring(any()) } returns listOf(recurring(1, occurrenceDate))
        coEvery {
            exchangeRateRepository.getRateForDate("EUR", "DKK", occurrenceDate)
        } returns ExchangeRate("EUR", "DKK", 7.46, occurrenceDate, Instant.fromEpochSeconds(0), "frankfurter")

        val captured = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(captured)) } returns 9L

        useCase()

        coVerify(exactly = 1) { exchangeRateRepository.getRateForDate("EUR", "DKK", occurrenceDate) }
        assertThat(captured.captured.exchangeRateUsed).isEqualTo(7.46)
        assertThat(captured.captured.homeAmount).isEqualTo(74600) // 100.00 EUR * 7.46
        assertThat(captured.captured.date).isEqualTo(occurrenceDate)
    }

    @Test
    fun `home-currency recurring does not query the exchange rate`() = runTest {
        val occurrenceDate = LocalDate(2026, 2, 1)
        coEvery { recurringExpenseRepository.getDueRecurring(any()) } returns
            listOf(recurring(1, occurrenceDate, code = "DKK", amount = 500000))

        useCase()

        coVerify(exactly = 0) { exchangeRateRepository.getRateForDate(any(), any(), any()) }
        coVerify { expenseRepository.addExpense(any()) }
    }

    @Test
    fun `occurrence is skipped when rate cannot be locked for its date`() = runTest {
        val occurrenceDate = LocalDate(2026, 3, 1)
        coEvery { recurringExpenseRepository.getDueRecurring(any()) } returns listOf(recurring(1, occurrenceDate))
        coEvery {
            exchangeRateRepository.getRateForDate("EUR", "DKK", occurrenceDate)
        } returns null

        useCase()

        coVerify(exactly = 0) { expenseRepository.addExpense(any()) }
        coVerify(exactly = 0) { recurringExpenseRepository.updateNextDate(any(), any()) }
    }

    @Test
    fun `each occurrence locks its own date-specific rate`() = runTest {
        val firstDate = LocalDate(2026, 1, 1)
        val secondDate = LocalDate(2026, 2, 1)
        coEvery { recurringExpenseRepository.getDueRecurring(any()) } returns listOf(
            recurring(1, firstDate),
            recurring(2, secondDate),
        )
        coEvery {
            exchangeRateRepository.getRateForDate("EUR", "DKK", firstDate)
        } returns ExchangeRate("EUR", "DKK", 7.40, firstDate, Instant.fromEpochSeconds(0), "frankfurter")
        coEvery {
            exchangeRateRepository.getRateForDate("EUR", "DKK", secondDate)
        } returns ExchangeRate("EUR", "DKK", 7.50, secondDate, Instant.fromEpochSeconds(0), "frankfurter")

        val captured = mutableListOf<Expense>()
        coEvery { expenseRepository.addExpense(capture(captured)) } returns 1L

        useCase()

        assertThat(captured).hasSize(2)
        assertThat(captured[0].homeAmount).isEqualTo(74000) // 100 EUR * 7.40
        assertThat(captured[1].homeAmount).isEqualTo(75000) // 100 EUR * 7.50
    }
}
