package com.sofato.krone.ui.expenses

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.expense.DeleteExpenseUseCase
import com.sofato.krone.domain.usecase.expense.GetRecentExpensesUseCase
import com.sofato.krone.domain.usecase.expense.RestoreExpenseUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val dkk = Currency(
        code = "DKK",
        name = "Danish Krone",
        symbol = "kr.",
        decimalPlaces = 2,
        symbolPosition = SymbolPosition.AFTER,
        isEnabled = true,
        sortOrder = 0,
    )

    private val category = Category(
        id = 1,
        name = "Food",
        iconName = "restaurant",
        colorHex = "#FF0000",
        isCustom = false,
        sortOrder = 0,
    )

    private lateinit var deleteExpenseUseCase: DeleteExpenseUseCase
    private lateinit var restoreExpenseUseCase: RestoreExpenseUseCase
    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        deleteExpenseUseCase = mockk(relaxed = true)
        restoreExpenseUseCase = mockk(relaxed = true)
        currencyRepository = mockk {
            coEvery { getCurrencyByCode(any()) } returns dkk
        }
        userPreferencesRepository = mockk {
            every { homeCurrencyCode } returns flowOf("DKK")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun expense(id: Long, date: LocalDate, createdAtEpoch: Long): Expense =
        Expense(
            id = id,
            amount = 10000,
            currency = dkk,
            homeAmount = 10000,
            exchangeRateUsed = 1.0,
            category = category,
            note = null,
            date = date,
            createdAt = Instant.fromEpochSeconds(createdAtEpoch),
        )

    private fun buildViewModel(
        getRecentExpensesUseCase: GetRecentExpensesUseCase,
    ): ExpenseListViewModel =
        ExpenseListViewModel(
            getRecentExpensesUseCase = getRecentExpensesUseCase,
            deleteExpenseUseCase = deleteExpenseUseCase,
            restoreExpenseUseCase = restoreExpenseUseCase,
            currencyRepository = currencyRepository,
            userPreferencesRepository = userPreferencesRepository,
        )

    @Test
    fun `grouped expenses are sorted by date descending`() = runTest {
        val april1 = LocalDate(2026, 4, 1)
        val april3 = LocalDate(2026, 4, 3)
        val april5 = LocalDate(2026, 4, 5)

        // Expenses ordered by createdAt DESC (as DB returns them).
        // Intentionally: the oldest date has the newest createdAt to trigger the bug.
        val expenses = listOf(
            expense(id = 1, date = april1, createdAtEpoch = 300),  // oldest date, created last
            expense(id = 2, date = april5, createdAtEpoch = 200),
            expense(id = 3, date = april3, createdAtEpoch = 100),  // newest create is oldest
        )

        val useCase = mockk<GetRecentExpensesUseCase> {
            every { this@mockk.invoke(any()) } returns flowOf(expenses)
        }

        val viewModel = buildViewModel(useCase)

        viewModel.groupedExpenses.test {
            val grouped = awaitItem()
            assertThat(grouped).isNotEmpty()
            val dates = grouped.keys.toList()
            assertThat(dates).isEqualTo(listOf(april5, april3, april1))
        }
    }

    @Test
    fun `grouped expenses with same date are kept in one group`() = runTest {
        val april3 = LocalDate(2026, 4, 3)

        val expenses = listOf(
            expense(id = 1, date = april3, createdAtEpoch = 300),
            expense(id = 2, date = april3, createdAtEpoch = 200),
            expense(id = 3, date = april3, createdAtEpoch = 100),
        )

        val useCase = mockk<GetRecentExpensesUseCase> {
            every { this@mockk.invoke(any()) } returns flowOf(expenses)
        }

        val viewModel = buildViewModel(useCase)

        viewModel.groupedExpenses.test {
            val grouped = awaitItem()
            assertThat(grouped.keys).hasSize(1)
            assertThat(grouped[april3]).hasSize(3)
        }
    }

    @Test
    fun `empty expense list produces empty map`() = runTest {
        val useCase = mockk<GetRecentExpensesUseCase> {
            every { this@mockk.invoke(any()) } returns flowOf(emptyList())
        }

        val viewModel = buildViewModel(useCase)

        viewModel.groupedExpenses.test {
            val grouped = awaitItem()
            assertThat(grouped).isEmpty()
        }
    }

    @Test
    fun `grouped expenses update when source emits new data`() = runTest {
        val april1 = LocalDate(2026, 4, 1)
        val april2 = LocalDate(2026, 4, 2)

        val source = MutableStateFlow(
            listOf(expense(id = 1, date = april1, createdAtEpoch = 100)),
        )

        val useCase = mockk<GetRecentExpensesUseCase> {
            every { this@mockk.invoke(any()) } returns source
        }

        val viewModel = buildViewModel(useCase)

        viewModel.groupedExpenses.test {
            val first = awaitItem()
            assertThat(first.keys.toList()).isEqualTo(listOf(april1))

            source.value = listOf(
                expense(id = 2, date = april2, createdAtEpoch = 200),
                expense(id = 1, date = april1, createdAtEpoch = 100),
            )

            val second = awaitItem()
            assertThat(second.keys.toList()).isEqualTo(listOf(april2, april1))
        }
    }
}
