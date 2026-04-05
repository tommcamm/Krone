package com.sofato.krone.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.BudgetOverview
import com.sofato.krone.domain.model.CategoryMonthlySpend
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.CurrencyBreakdownItem
import com.sofato.krone.domain.model.DailyBudget
import com.sofato.krone.domain.model.DailySpend
import com.sofato.krone.domain.model.MonthlySnapshot
import com.sofato.krone.domain.model.SpendingStreak
import com.sofato.krone.domain.model.InsightData
import com.sofato.krone.domain.model.TextInsight
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.MonthlySnapshotRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.usecase.budget.CalculateDailyBudgetUseCase
import com.sofato.krone.domain.usecase.budget.GetBudgetOverviewUseCase
import com.sofato.krone.domain.usecase.insights.GenerateTextInsightsUseCase
import com.sofato.krone.domain.usecase.insights.GetCategoryComparisonUseCase
import com.sofato.krone.domain.usecase.insights.GetCurrencyBreakdownUseCase
import com.sofato.krone.domain.usecase.insights.GetDailySpendingUseCase
import com.sofato.krone.domain.usecase.insights.GetSpendingStreakUseCase
import com.sofato.krone.domain.usecase.insights.GetSpendingTrendUseCase
import com.sofato.krone.util.startOfMonth
import com.sofato.krone.util.today
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    getDailySpending: GetDailySpendingUseCase,
    getCategoryComparison: GetCategoryComparisonUseCase,
    getCurrencyBreakdown: GetCurrencyBreakdownUseCase,
    getSpendingStreak: GetSpendingStreakUseCase,
    private val generateTextInsights: GenerateTextInsightsUseCase,
    getSpendingTrend: GetSpendingTrendUseCase,
    getBudgetOverview: GetBudgetOverviewUseCase,
    calculateDailyBudget: CalculateDailyBudgetUseCase,
    userPreferencesRepository: UserPreferencesRepository,
    currencyRepository: CurrencyRepository,
    monthlySnapshotRepository: MonthlySnapshotRepository,
) : ViewModel() {

    val homeCurrency: StateFlow<Currency?> =
        userPreferencesRepository.homeCurrencyCode
            .flatMapLatest { code ->
                currencyRepository.getEnabledCurrencies()
                    .combine(flowOf(code)) { currencies, homeCode ->
                        currencies.find { it.code == homeCode }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyBudget: StateFlow<DailyBudget?> =
        calculateDailyBudget()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val budgetOverview: StateFlow<BudgetOverview?> =
        getBudgetOverview()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedHeatmapMonth = MutableStateFlow(LocalDate.today().startOfMonth())
    val selectedHeatmapMonth: StateFlow<LocalDate> = _selectedHeatmapMonth.asStateFlow()

    val heatmapDailySpending: StateFlow<List<DailySpend>> =
        _selectedHeatmapMonth.flatMapLatest { monthStart ->
            val monthEnd = monthStart.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            getDailySpending(startDate = monthStart, endDate = monthEnd)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySpending: StateFlow<List<DailySpend>> =
        getDailySpending()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryComparison: StateFlow<List<CategoryMonthlySpend>> =
        getCategoryComparison()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currencyBreakdown: StateFlow<List<CurrencyBreakdownItem>> =
        getCurrencyBreakdown()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streak: StateFlow<SpendingStreak> =
        getSpendingStreak()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingStreak(0, 0))

    val spendingTrend: StateFlow<List<GetSpendingTrendUseCase.MonthlyTrend>> =
        getSpendingTrend()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val snapshots: StateFlow<List<MonthlySnapshot>> =
        monthlySnapshotRepository.getAllSnapshots()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val insightData: StateFlow<List<InsightData>> =
        combine(categoryComparison, snapshots, streak) { cats, snaps, str ->
            generateTextInsights(cats, snaps, str)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectHeatmapMonth(month: LocalDate) {
        _selectedHeatmapMonth.value = month.startOfMonth()
    }

    fun previousHeatmapMonth() {
        _selectedHeatmapMonth.value = _selectedHeatmapMonth.value.minus(1, DateTimeUnit.MONTH)
    }

    fun nextHeatmapMonth() {
        _selectedHeatmapMonth.value = _selectedHeatmapMonth.value.plus(1, DateTimeUnit.MONTH)
    }
}
