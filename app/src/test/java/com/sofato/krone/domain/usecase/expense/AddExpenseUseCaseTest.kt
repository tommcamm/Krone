package com.sofato.krone.domain.usecase.expense

import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.usecase.currency.ConversionResult
import com.sofato.krone.domain.usecase.currency.ConvertAmountUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test

class AddExpenseUseCaseTest {

    private val dkk = Currency("DKK", "Danish Krone", "kr.", 2, SymbolPosition.AFTER, true, 0)
    private val eur = Currency("EUR", "Euro", "€", 2, SymbolPosition.BEFORE, true, 1)
    private val food = Category(1, "Food", "restaurant", "#FF0000", false, 0)

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var convertAmountUseCase: ConvertAmountUseCase
    private lateinit var useCase: AddExpenseUseCase

    @Before
    fun setUp() {
        expenseRepository = mockk()
        convertAmountUseCase = mockk()
        useCase = AddExpenseUseCase(expenseRepository, convertAmountUseCase)

        coEvery { expenseRepository.addExpense(any()) } returns 42L
    }

    @Test
    fun `stores the converted home amount and rate returned by the conversion`() = runTest {
        val date = LocalDate(2024, 1, 15)
        coEvery { convertAmountUseCase(10000, "EUR", date) } returns
            ConversionResult(convertedAmountMinor = 74500, rateUsed = 7.45)

        val captured = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(captured)) } returns 7L

        val result = useCase(
            amountMinor = 10000, // 100.00 EUR
            currency = eur,
            category = food,
            note = null,
            date = date,
        )

        assertThat(result).isEqualTo(7L)
        assertThat(captured.captured.amount).isEqualTo(10000)
        assertThat(captured.captured.homeAmount).isEqualTo(74500)
        assertThat(captured.captured.exchangeRateUsed).isEqualTo(7.45)
        assertThat(captured.captured.date).isEqualTo(date)
        coVerify(exactly = 1) { convertAmountUseCase(10000, "EUR", date) }
    }

    @Test
    fun `home-currency path stores amount 1 to 1 via the identity conversion`() = runTest {
        val date = LocalDate(2026, 4, 19)
        coEvery { convertAmountUseCase(15000, "DKK", date) } returns
            ConversionResult(convertedAmountMinor = 15000, rateUsed = 1.0)

        val captured = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(captured)) } returns 1L

        val result = useCase(
            amountMinor = 15000,
            currency = dkk,
            category = food,
            note = null,
            date = date,
        )

        assertThat(result).isEqualTo(1L)
        assertThat(captured.captured.homeAmount).isEqualTo(15000)
        assertThat(captured.captured.exchangeRateUsed).isEqualTo(1.0)
    }

    @Test
    fun `returns null when conversion is unavailable for the transaction's date`() = runTest {
        val date = LocalDate(2026, 4, 19)
        coEvery { convertAmountUseCase(10000, "EUR", date) } returns null

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
    fun `blank note is normalized to null`() = runTest {
        val date = LocalDate(2026, 4, 19)
        coEvery { convertAmountUseCase(any(), any(), any()) } returns
            ConversionResult(convertedAmountMinor = 1000, rateUsed = 1.0)

        val captured = slot<Expense>()
        coEvery { expenseRepository.addExpense(capture(captured)) } returns 1L

        useCase(
            amountMinor = 1000,
            currency = dkk,
            category = food,
            note = "   ",
            date = date,
        )

        assertThat(captured.captured.note).isNull()
    }
}
