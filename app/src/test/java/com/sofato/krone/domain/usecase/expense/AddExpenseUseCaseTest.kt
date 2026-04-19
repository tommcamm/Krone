package com.sofato.krone.domain.usecase.expense

import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.ExchangeRate
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

class AddExpenseUseCaseTest {

    private val dkk = Currency("DKK", "Danish Krone", "kr.", 2, SymbolPosition.AFTER, true, 0)
    private val eur = Currency("EUR", "Euro", "€", 2, SymbolPosition.BEFORE, true, 1)
    private val food = Category(1, "Food", "restaurant", "#FF0000", false, 0)

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var exchangeRateRepository: ExchangeRateRepository
    private lateinit var useCase: AddExpenseUseCase

    @Before
    fun setUp() {
        expenseRepository = mockk()
        userPreferencesRepository = mockk {
            every { homeCurrencyCode } returns flowOf("DKK")
        }
        exchangeRateRepository = mockk()
        useCase = AddExpenseUseCase(expenseRepository, userPreferencesRepository, exchangeRateRepository)

        coEvery { expenseRepository.addExpense(any()) } returns 42L
    }

    @Test
    fun `home-currency expense stores amount 1 to 1 without touching exchange rate`() = runTest {
        val captured = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(captured)) } returns 1L

        val result = useCase(
            amountMinor = 15000,
            currency = dkk,
            category = food,
            note = null,
            date = LocalDate(2026, 4, 19),
        )

        assertThat(result).isEqualTo(1L)
        assertThat(captured.captured.amount).isEqualTo(15000)
        assertThat(captured.captured.homeAmount).isEqualTo(15000)
        assertThat(captured.captured.exchangeRateUsed).isEqualTo(1.0)
        coVerify(exactly = 0) { exchangeRateRepository.getRateForDate(any(), any(), any()) }
    }

    @Test
    fun `foreign currency locks rate for transaction's date, not today`() = runTest {
        // Distinctly different rates: 7.45 on the transaction date vs 8.00 today.
        val backDate = LocalDate(2024, 1, 15)
        coEvery {
            exchangeRateRepository.getRateForDate("EUR", "DKK", backDate)
        } returns ExchangeRate("EUR", "DKK", 7.45, backDate, Instant.fromEpochSeconds(0), "frankfurter")

        val captured = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(captured)) } returns 7L

        val result = useCase(
            amountMinor = 10000, // 100.00 EUR
            currency = eur,
            category = food,
            note = null,
            date = backDate,
        )

        assertThat(result).isEqualTo(7L)
        assertThat(captured.captured.exchangeRateUsed).isEqualTo(7.45)
        assertThat(captured.captured.homeAmount).isEqualTo(74500) // 100.00 EUR * 7.45
        coVerify(exactly = 1) { exchangeRateRepository.getRateForDate("EUR", "DKK", backDate) }
    }

    @Test
    fun `rate lookup uses the exact date the user picked`() = runTest {
        val pickedDate = LocalDate(2025, 11, 3)
        coEvery {
            exchangeRateRepository.getRateForDate("EUR", "DKK", pickedDate)
        } returns ExchangeRate("EUR", "DKK", 7.50, pickedDate, Instant.fromEpochSeconds(0), "frankfurter")
        coEvery { expenseRepository.addExpense(any()) } returns 1L

        useCase(
            amountMinor = 5000,
            currency = eur,
            category = food,
            note = null,
            date = pickedDate,
        )

        coVerify(exactly = 1) { exchangeRateRepository.getRateForDate("EUR", "DKK", pickedDate) }
    }

    @Test
    fun `returns null when rate is unavailable for the transaction's date`() = runTest {
        val date = LocalDate(2026, 4, 19)
        coEvery {
            exchangeRateRepository.getRateForDate("EUR", "DKK", date)
        } returns null

        val result = useCase(
            amountMinor = 10000,
            currency = eur,
            category = food,
            note = null,
            date = date,
        )

        assertThat(result).isNull()
        coVerify(exactly = 0) { expenseRepository.addExpense(any()) }
    }

    @Test
    fun `home-currency path stays correct even when the transaction is back-dated`() = runTest {
        val captured = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(captured)) } returns 1L

        useCase(
            amountMinor = 5000,
            currency = dkk,
            category = food,
            note = null,
            date = LocalDate(2020, 6, 1),
        )

        assertThat(captured.captured.exchangeRateUsed).isEqualTo(1.0)
        coVerify(exactly = 0) { exchangeRateRepository.getRateForDate(any(), any(), any()) }
    }
}
