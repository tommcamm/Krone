package com.sofato.krone.ui.dashboard

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.BudgetOverview
import com.sofato.krone.domain.model.BudgetPeriod
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.DailyBudget
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.budget.CalculateBudgetPeriodUseCase
import com.sofato.krone.domain.usecase.budget.CalculateDailyBudgetUseCase
import com.sofato.krone.domain.usecase.budget.GetBudgetOverviewUseCase
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.expense.GetExpensesByDateUseCase
import com.sofato.krone.domain.usecase.expense.GetRecentExpensesUseCase
import com.sofato.krone.domain.usecase.recurring.ProcessDueRecurringExpensesUseCase
import com.sofato.krone.domain.usecase.savings.ProcessSavingsContributionsUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

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

    private val period = BudgetPeriod(
        startDate = LocalDate(2026, 4, 1),
        endDate = LocalDate(2026, 4, 30),
    )

    private lateinit var getExpensesByDateUseCase: GetExpensesByDateUseCase
    private lateinit var getRecentExpensesUseCase: GetRecentExpensesUseCase
    private lateinit var calculateDailyBudgetUseCase: CalculateDailyBudgetUseCase
    private lateinit var calculateBudgetPeriodUseCase: CalculateBudgetPeriodUseCase
    private lateinit var getBudgetOverviewUseCase: GetBudgetOverviewUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var processRecurringUseCase: ProcessDueRecurringExpensesUseCase
    private lateinit var processSavingsUseCase: ProcessSavingsContributionsUseCase
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var currencyRepository: CurrencyRepository

    private val defaultDailyBudget = dailyBudget()
    private lateinit var dailyBudgetFlow: MutableStateFlow<DailyBudget>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        dailyBudgetFlow = MutableStateFlow(defaultDailyBudget)

        getExpensesByDateUseCase = mockk {
            every { this@mockk.invoke(any()) } returns flowOf(emptyList())
        }
        getRecentExpensesUseCase = mockk {
            every { this@mockk.invoke(any()) } returns flowOf(emptyList())
        }
        calculateDailyBudgetUseCase = mockk {
            every { this@mockk.invoke() } returns dailyBudgetFlow
        }
        calculateBudgetPeriodUseCase = mockk {
            coEvery { this@mockk.invoke(any()) } returns period
        }
        getBudgetOverviewUseCase = mockk {
            every { this@mockk.invoke() } returns flowOf(
                BudgetOverview(
                    period = period,
                    totalIncomeMinor = 200000,
                    totalFixedMinor = 80000,
                    totalSavingsMinor = 20000,
                    discretionaryMinor = 100000,
                    spentMinor = 30000,
                    categoryBreakdown = emptyList(),
                    currencyCode = "DKK",
                ),
            )
        }
        getCategoriesUseCase = mockk {
            every { this@mockk.invoke() } returns flowOf(emptyList())
        }
        processRecurringUseCase = mockk(relaxed = true)
        processSavingsUseCase = mockk(relaxed = true)
        userPreferencesRepository = mockk {
            every { homeCurrencyCode } returns flowOf("DKK")
        }
        currencyRepository = mockk {
            every { getEnabledCurrencies() } returns flowOf(listOf(dkk))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): DashboardViewModel =
        DashboardViewModel(
            getExpensesByDate = getExpensesByDateUseCase,
            getRecentExpensesUseCase = getRecentExpensesUseCase,
            calculateBudgetPeriodUseCase = calculateBudgetPeriodUseCase,
            processRecurringUseCase = processRecurringUseCase,
            processSavingsUseCase = processSavingsUseCase,
            calculateDailyBudgetUseCase = calculateDailyBudgetUseCase,
            getBudgetOverviewUseCase = getBudgetOverviewUseCase,
            getCategoriesUseCase = getCategoriesUseCase,
            userPreferencesRepository = userPreferencesRepository,
            currencyRepository = currencyRepository,
        )

    private fun dailyBudget(
        dailyAmount: Long = 5000,
        spentSoFar: Long = 30000,
        remainingDays: Int = 20,
        discretionary: Long = 100000,
    ) = DailyBudget(
        dailyAmountMinor = dailyAmount,
        totalIncomeMinor = 200000,
        totalFixedMinor = 80000,
        totalSavingsMinor = 20000,
        spentSoFarMinor = spentSoFar,
        remainingDays = remainingDays,
        discretionaryMinor = discretionary,
        currencyCode = "DKK",
    )

    // --- projectedEndOfMonth tests ---

    @Test
    fun `projection is negative when under budget`() = runTest {
        dailyBudgetFlow.value = dailyBudget(
            spentSoFar = 20000,    // 200 DKK spent
            remainingDays = 20,
            discretionary = 100000, // 1000 DKK discretionary
        )

        val viewModel = buildViewModel()

        viewModel.projectedEndOfMonth.test {
            val projection = awaitItem()
            // rollingAvg = (20000 + 0) / (30 - 20 + 1) = 20000 / 11 ≈ 1818
            // projected = 20000 + 1818 * 20 = 56360
            // difference = 56360 - 100000 = -43640 (under budget)
            assertThat(projection).isLessThan(0)
        }
    }

    @Test
    fun `projection is positive when over budget`() = runTest {
        dailyBudgetFlow.value = dailyBudget(
            spentSoFar = 80000,    // 800 DKK spent
            remainingDays = 20,
            discretionary = 100000, // 1000 DKK discretionary
        )

        val viewModel = buildViewModel()

        viewModel.projectedEndOfMonth.test {
            val projection = awaitItem()
            // rollingAvg = 80000 / 11 ≈ 7272
            // projected = 80000 + 7272 * 20 = 225440
            // difference = 225440 - 100000 = 125440 (over budget)
            assertThat(projection).isGreaterThan(0)
        }
    }

    @Test
    fun `projection equals spent minus discretionary when zero remaining days`() = runTest {
        dailyBudgetFlow.value = dailyBudget(
            spentSoFar = 90000,
            remainingDays = 0,
            discretionary = 100000,
        )

        val viewModel = buildViewModel()

        viewModel.projectedEndOfMonth.test {
            val projection = awaitItem()
            // rollingAvg = 0 (remainingDays <= 0)
            // projected = 90000 + 0 * 0 = 90000
            // difference = 90000 - 100000 = -10000
            assertThat(projection).isEqualTo(-10000L)
        }
    }

    // --- rollingDailyAverage tests ---

    @Test
    fun `rolling average divides total spent by elapsed days`() = runTest {
        dailyBudgetFlow.value = dailyBudget(
            spentSoFar = 50000,
            remainingDays = 20,
        )

        val viewModel = buildViewModel()

        viewModel.rollingDailyAverage.test {
            val avg = awaitItem()
            // period.totalDays = 30, remainingDays = 20, elapsed = 30 - 20 + 1 = 11
            // totalSpent = 50000 + 0 (no today's expenses) = 50000
            // avg = 50000 / 11 = 4545
            assertThat(avg).isEqualTo(50000L / 11)
        }
    }

    @Test
    fun `rolling average is zero when zero remaining days`() = runTest {
        dailyBudgetFlow.value = dailyBudget(remainingDays = 0)

        val viewModel = buildViewModel()

        viewModel.rollingDailyAverage.test {
            assertThat(awaitItem()).isEqualTo(0L)
        }
    }

    // --- homeCurrency tests ---

    @Test
    fun `home currency resolves from preferences`() = runTest {
        val viewModel = buildViewModel()

        viewModel.homeCurrency.test {
            assertThat(awaitItem()).isEqualTo(dkk)
        }
    }

    // --- totalSpentToday tests ---

    @Test
    fun `today spent excludes recurring instances`() = runTest {
        val recurringExpense = mockk<Expense> {
            every { isRecurringInstance } returns true
            every { homeAmount } returns 10000L
        }
        val manualExpense = mockk<Expense> {
            every { isRecurringInstance } returns false
            every { homeAmount } returns 5000L
        }

        getExpensesByDateUseCase = mockk {
            every { this@mockk.invoke(any()) } returns flowOf(listOf(recurringExpense, manualExpense))
        }

        val viewModel = buildViewModel()

        viewModel.totalSpentToday.test {
            assertThat(awaitItem()).isEqualTo(5000L)
        }
    }
}
