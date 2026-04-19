package com.sofato.krone.domain.usecase.budget

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.BudgetPeriod
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.IncomeRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GetBudgetOverviewUseCaseTest {

    private val dkk = Currency(
        code = "DKK",
        name = "Danish Krone",
        symbol = "kr.",
        decimalPlaces = 2,
        symbolPosition = SymbolPosition.AFTER,
        isEnabled = true,
        sortOrder = 0,
    )

    private val period = BudgetPeriod(
        startDate = LocalDate(2026, 4, 1),
        endDate = LocalDate(2026, 4, 30),
    )

    private val food = Category(
        id = 1,
        name = "Food",
        iconName = "restaurant",
        colorHex = "#FF0000",
        isCustom = false,
        sortOrder = 0,
    )
    private val housing = Category(
        id = 2,
        name = "Housing",
        iconName = "home",
        colorHex = "#00FF00",
        isCustom = false,
        sortOrder = 1,
    )

    private lateinit var calculateBudgetPeriodUseCase: CalculateBudgetPeriodUseCase
    private lateinit var getOrCopyForwardAllocationsUseCase: GetOrCopyForwardAllocationsUseCase
    private lateinit var incomeRepository: IncomeRepository
    private lateinit var recurringExpenseRepository: RecurringExpenseRepository
    private lateinit var savingsBucketRepository: SavingsBucketRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    @Before
    fun setUp() {
        calculateBudgetPeriodUseCase = mockk {
            coEvery { this@mockk.invoke(any()) } returns period
        }
        getOrCopyForwardAllocationsUseCase = mockk {
            every { this@mockk.invoke(any()) } returns flowOf(emptyList())
        }
        incomeRepository = mockk {
            every { getTotalRecurringIncomeMinor() } returns flowOf(300_000L)
        }
        recurringExpenseRepository = mockk {
            every { getTotalActiveRecurringMinor() } returns flowOf(80_000L)
        }
        savingsBucketRepository = mockk {
            every { getTotalMonthlyContributionsMinor() } returns flowOf(20_000L)
        }
        categoryRepository = mockk {
            every { getActiveCategories() } returns flowOf(listOf(food, housing))
        }
        userPreferencesRepository = mockk {
            every { homeCurrencyCode } returns flowOf("DKK")
        }
        expenseRepository = mockk()
    }

    private fun expense(
        id: Long,
        category: Category,
        homeAmount: Long,
        isRecurringInstance: Boolean = false,
        date: LocalDate = LocalDate(2026, 4, 15),
    ) = Expense(
        id = id,
        amount = homeAmount,
        currency = dkk,
        homeAmount = homeAmount,
        exchangeRateUsed = 1.0,
        category = category,
        note = null,
        date = date,
        createdAt = Instant.fromEpochSeconds(1_000L + id),
        isRecurringInstance = isRecurringInstance,
        recurringExpenseId = if (isRecurringInstance) 1L else null,
    )

    private fun useCase() = GetBudgetOverviewUseCase(
        calculateBudgetPeriod = calculateBudgetPeriodUseCase,
        getOrCopyForwardAllocations = getOrCopyForwardAllocationsUseCase,
        incomeRepository = incomeRepository,
        recurringExpenseRepository = recurringExpenseRepository,
        savingsBucketRepository = savingsBucketRepository,
        expenseRepository = expenseRepository,
        categoryRepository = categoryRepository,
        userPreferencesRepository = userPreferencesRepository,
    )

    @Test
    fun `categoryBreakdown excludes recurring-instance expenses`() = runTest {
        // Housing: 1 recurring (rent, 80k) + 1 discretionary (IKEA, 2k) → breakdown should show 2k.
        // Food: 2 discretionary (5k + 3k) → breakdown should show 8k.
        every { expenseRepository.getExpensesBetween(any(), any()) } returns flowOf(
            listOf(
                expense(id = 1, category = housing, homeAmount = 80_000, isRecurringInstance = true),
                expense(id = 2, category = housing, homeAmount = 2_000),
                expense(id = 3, category = food, homeAmount = 5_000),
                expense(id = 4, category = food, homeAmount = 3_000),
            ),
        )

        useCase().invoke().test {
            val overview = awaitItem()
            val byCategory = overview.categoryBreakdown.associate { it.category.id to it.spentMinor }
            assertThat(byCategory[food.id]).isEqualTo(8_000L)
            assertThat(byCategory[housing.id]).isEqualTo(2_000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `total spentMinor excludes recurring-instance expenses`() = runTest {
        every { expenseRepository.getExpensesBetween(any(), any()) } returns flowOf(
            listOf(
                expense(id = 1, category = housing, homeAmount = 80_000, isRecurringInstance = true),
                expense(id = 2, category = food, homeAmount = 5_000),
                expense(id = 3, category = food, homeAmount = 3_000),
            ),
        )

        useCase().invoke().test {
            val overview = awaitItem()
            assertThat(overview.spentMinor).isEqualTo(8_000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `category with only recurring expenses is omitted from breakdown when no allocation`() = runTest {
        every { expenseRepository.getExpensesBetween(any(), any()) } returns flowOf(
            listOf(
                expense(id = 1, category = housing, homeAmount = 80_000, isRecurringInstance = true),
                expense(id = 2, category = food, homeAmount = 5_000),
            ),
        )

        useCase().invoke().test {
            val overview = awaitItem()
            assertThat(overview.categoryBreakdown.map { it.category.id }).containsExactly(food.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `discretionary and fixed totals are unchanged by recurring filter`() = runTest {
        every { expenseRepository.getExpensesBetween(any(), any()) } returns flowOf(
            listOf(
                expense(id = 1, category = housing, homeAmount = 80_000, isRecurringInstance = true),
            ),
        )

        useCase().invoke().test {
            val overview = awaitItem()
            // income 300k − fixed 80k − savings 20k = 200k discretionary
            assertThat(overview.totalIncomeMinor).isEqualTo(300_000L)
            assertThat(overview.totalFixedMinor).isEqualTo(80_000L)
            assertThat(overview.totalSavingsMinor).isEqualTo(20_000L)
            assertThat(overview.discretionaryMinor).isEqualTo(200_000L)
            // And the recurring rent did not land in spent.
            assertThat(overview.spentMinor).isEqualTo(0L)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
