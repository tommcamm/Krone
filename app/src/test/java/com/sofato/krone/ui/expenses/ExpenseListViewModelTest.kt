package com.sofato.krone.ui.expenses

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.expense.DeleteExpenseUseCase
import com.sofato.krone.domain.usecase.expense.GetAllExpensesUseCase
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
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase

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
        getCategoriesUseCase = mockk {
            every { this@mockk.invoke() } returns flowOf(listOf(category))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun expense(
        id: Long,
        date: LocalDate,
        createdAtEpoch: Long,
        homeAmount: Long = 10000,
    ): Expense = Expense(
        id = id,
        amount = homeAmount,
        currency = dkk,
        homeAmount = homeAmount,
        exchangeRateUsed = 1.0,
        category = category,
        note = null,
        date = date,
        createdAt = Instant.fromEpochSeconds(createdAtEpoch),
    )

    private fun buildViewModel(
        getAllExpensesUseCase: GetAllExpensesUseCase,
    ): ExpenseListViewModel =
        ExpenseListViewModel(
            getAllExpensesUseCase = getAllExpensesUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            deleteExpenseUseCase = deleteExpenseUseCase,
            restoreExpenseUseCase = restoreExpenseUseCase,
            currencyRepository = currencyRepository,
            userPreferencesRepository = userPreferencesRepository,
        )

    @Test
    fun `default sort orders by date then createdAt descending`() = runTest {
        val april1 = LocalDate(2026, 4, 1)
        val april3 = LocalDate(2026, 4, 3)
        val april5 = LocalDate(2026, 4, 5)

        val expenses = listOf(
            expense(id = 1, date = april1, createdAtEpoch = 300),
            expense(id = 2, date = april5, createdAtEpoch = 200),
            expense(id = 3, date = april3, createdAtEpoch = 100),
        )

        val useCase = mockk<GetAllExpensesUseCase> {
            every { this@mockk.invoke() } returns flowOf(expenses)
        }

        val viewModel = buildViewModel(useCase)

        viewModel.expenses.test {
            val sorted = awaitItem()
            assertThat(sorted.map { it.id }).isEqualTo(listOf(2L, 3L, 1L))
        }
    }

    @Test
    fun `empty source produces empty list`() = runTest {
        val useCase = mockk<GetAllExpensesUseCase> {
            every { this@mockk.invoke() } returns flowOf(emptyList())
        }

        val viewModel = buildViewModel(useCase)

        viewModel.expenses.test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `expenses update when source emits new data`() = runTest {
        val april1 = LocalDate(2026, 4, 1)
        val april2 = LocalDate(2026, 4, 2)

        val source = MutableStateFlow(
            listOf(expense(id = 1, date = april1, createdAtEpoch = 100)),
        )

        val useCase = mockk<GetAllExpensesUseCase> {
            every { this@mockk.invoke() } returns source
        }

        val viewModel = buildViewModel(useCase)

        viewModel.expenses.test {
            val first = awaitItem()
            assertThat(first.map { it.id }).isEqualTo(listOf(1L))

            source.value = listOf(
                expense(id = 2, date = april2, createdAtEpoch = 200),
                expense(id = 1, date = april1, createdAtEpoch = 100),
            )

            val second = awaitItem()
            assertThat(second.map { it.id }).isEqualTo(listOf(2L, 1L))
        }
    }

    @Test
    fun `amount high sort orders by home amount descending`() = runTest {
        val day = LocalDate(2026, 4, 1)
        val expenses = listOf(
            expense(id = 1, date = day, createdAtEpoch = 100, homeAmount = 500),
            expense(id = 2, date = day, createdAtEpoch = 200, homeAmount = 9000),
            expense(id = 3, date = day, createdAtEpoch = 300, homeAmount = 1500),
        )
        val useCase = mockk<GetAllExpensesUseCase> {
            every { this@mockk.invoke() } returns flowOf(expenses)
        }

        val viewModel = buildViewModel(useCase)
        viewModel.updateSort(ExpenseSort.AmountHigh)

        viewModel.expenses.test {
            // Initial emit (DateNewest) may arrive first; skip to the one with new sort.
            val items = awaitItem()
            val final = if (items.map { it.id } == listOf(2L, 3L, 1L)) items else awaitItem()
            assertThat(final.map { it.id }).isEqualTo(listOf(2L, 3L, 1L))
        }
    }

    @Test
    fun `category filter restricts results to matching ids`() = runTest {
        val day = LocalDate(2026, 4, 1)
        val otherCategory = category.copy(id = 2, name = "Transport")
        val expenses = listOf(
            expense(id = 1, date = day, createdAtEpoch = 100),
            Expense(
                id = 2,
                amount = 10000,
                currency = dkk,
                homeAmount = 10000,
                exchangeRateUsed = 1.0,
                category = otherCategory,
                note = null,
                date = day,
                createdAt = Instant.fromEpochSeconds(200),
            ),
        )
        val useCase = mockk<GetAllExpensesUseCase> {
            every { this@mockk.invoke() } returns flowOf(expenses)
        }

        val viewModel = buildViewModel(useCase)
        viewModel.updateFilter(ExpenseFilter(categoryIds = setOf(2L)))

        viewModel.expenses.test {
            val items = awaitItem()
            val final = if (items.map { it.id } == listOf(2L)) items else awaitItem()
            assertThat(final.map { it.id }).isEqualTo(listOf(2L))
        }
    }
}
