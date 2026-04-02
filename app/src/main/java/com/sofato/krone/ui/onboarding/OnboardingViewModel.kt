package com.sofato.krone.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Income
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.model.SavingsBucketType
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.sofato.krone.util.CurrencyFormatter
import com.sofato.krone.util.today
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class OnboardingFixedExpense(
    val label: String,
    val amountMinor: Long,
    val categoryId: Long,
    val iconName: String,
    val colorHex: String,
)

data class OnboardingSavingsGoal(
    val label: String,
    val type: SavingsBucketType,
    val monthlyContributionMinor: Long,
    val targetAmountMinor: Long?,
)

data class OnboardingResult(
    val incomeMinor: Long,
    val totalFixedMinor: Long,
    val totalSavingsMinor: Long,
    val dailyBudgetMinor: Long,
    val currencyCode: String,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    currencyRepository: CurrencyRepository,
    getCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    val totalSteps = 5 // 0=currency, 1=income, 2=expenses, 3=savings, 4=result

    // Step 0: Currency & payday
    private val _selectedCurrencyCode = MutableStateFlow("DKK")
    val selectedCurrencyCode: StateFlow<String> = _selectedCurrencyCode.asStateFlow()

    private val _incomeDay = MutableStateFlow(1)
    val incomeDay: StateFlow<Int> = _incomeDay.asStateFlow()

    // Step 1: Income
    private val _incomeAmount = MutableStateFlow("")
    val incomeAmount: StateFlow<String> = _incomeAmount.asStateFlow()

    private val _incomeLabel = MutableStateFlow("Salary")
    val incomeLabel: StateFlow<String> = _incomeLabel.asStateFlow()

    // Step 2: Fixed expenses - pre-populated with Danish categories
    private val _fixedExpenses = MutableStateFlow(
        listOf(
            OnboardingFixedExpense("Rent", 0L, 0L, "Home", "#FF607D8B"),
            OnboardingFixedExpense("Electricity", 0L, 0L, "Bolt", "#FFFFC107"),
            OnboardingFixedExpense("Heating", 0L, 0L, "Whatshot", "#FFFF5722"),
            OnboardingFixedExpense("Water", 0L, 0L, "WaterDrop", "#FF2196F3"),
            OnboardingFixedExpense("Internet & mobile", 0L, 0L, "Wifi", "#FF3F51B5"),
            OnboardingFixedExpense("Insurance", 0L, 0L, "Shield", "#FF4CAF50"),
            OnboardingFixedExpense("Transport", 0L, 0L, "DirectionsBus", "#FF2196F3"),
            OnboardingFixedExpense("A-kasse", 0L, 0L, "WorkOutline", "#FF9C27B0"),
            OnboardingFixedExpense("Subscriptions", 0L, 0L, "Subscriptions", "#FFE91E63"),
            OnboardingFixedExpense("Loan repayments", 0L, 0L, "AccountBalance", "#FF795548"),
        )
    )
    val fixedExpenses: StateFlow<List<OnboardingFixedExpense>> = _fixedExpenses.asStateFlow()

    // Step 3: Savings goals
    private val _savingsGoals = MutableStateFlow<List<OnboardingSavingsGoal>>(emptyList())
    val savingsGoals: StateFlow<List<OnboardingSavingsGoal>> = _savingsGoals.asStateFlow()

    // Currencies
    val enabledCurrencies: StateFlow<List<Currency>> =
        currencyRepository.getEnabledCurrencies()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computed result preview
    val resultPreview: StateFlow<OnboardingResult> =
        combine(
            _incomeAmount,
            _fixedExpenses,
            _savingsGoals,
            _selectedCurrencyCode,
        ) { amountText, expenses, goals, currencyCode ->
            val incomeMinor = CurrencyFormatter.parseToMinorUnits(amountText, 2) ?: 0L
            val totalFixed = expenses.sumOf { it.amountMinor }
            val totalSavings = goals.sumOf { it.monthlyContributionMinor }
            val discretionary = incomeMinor - totalFixed - totalSavings
            val daily = if (discretionary > 0) discretionary / 30 else 0L
            OnboardingResult(
                incomeMinor = incomeMinor,
                totalFixedMinor = totalFixed,
                totalSavingsMinor = totalSavings,
                dailyBudgetMinor = daily,
                currencyCode = currencyCode,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            OnboardingResult(0L, 0L, 0L, 0L, "DKK"),
        )

    // Events
    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    sealed interface OnboardingEvent {
        data object Completed : OnboardingEvent
    }

    // Navigation
    fun nextStep() {
        if (_currentStep.value < 4) {
            _currentStep.value += 1
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value -= 1
        }
    }

    // Step 0 actions
    fun onCurrencySelected(code: String) {
        _selectedCurrencyCode.value = code
    }

    fun onIncomeDayChanged(day: Int) {
        _incomeDay.value = day.coerceIn(1, 31)
    }

    // Step 1 actions
    fun onIncomeAmountChanged(text: String) {
        _incomeAmount.value = text.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun onIncomeLabelChanged(text: String) {
        _incomeLabel.value = text
    }

    // Step 2 actions
    fun updateFixedExpenseAmount(index: Int, text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }
        val minor = CurrencyFormatter.parseToMinorUnits(filtered, 2) ?: 0L
        _fixedExpenses.value = _fixedExpenses.value.toMutableList().also {
            if (index in it.indices) {
                it[index] = it[index].copy(amountMinor = minor)
            }
        }
    }

    // Step 3 actions
    fun addSavingsGoal(
        label: String,
        type: SavingsBucketType,
        monthlyMinor: Long,
        targetMinor: Long?,
    ) {
        _savingsGoals.value = _savingsGoals.value + OnboardingSavingsGoal(
            label = label,
            type = type,
            monthlyContributionMinor = monthlyMinor,
            targetAmountMinor = targetMinor,
        )
    }

    fun removeSavingsGoal(index: Int) {
        _savingsGoals.value = _savingsGoals.value.toMutableList().also {
            if (index in it.indices) it.removeAt(index)
        }
    }

    fun updateSavingsGoalMonthly(index: Int, text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }
        val minor = CurrencyFormatter.parseToMinorUnits(filtered, 2) ?: 0L
        _savingsGoals.value = _savingsGoals.value.toMutableList().also {
            if (index in it.indices) {
                it[index] = it[index].copy(monthlyContributionMinor = minor)
            }
        }
    }

    fun updateSavingsGoalTarget(index: Int, text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }
        val minor = CurrencyFormatter.parseToMinorUnits(filtered, 2)
        _savingsGoals.value = _savingsGoals.value.toMutableList().also {
            if (index in it.indices) {
                it[index] = it[index].copy(targetAmountMinor = minor)
            }
        }
    }

    // Complete onboarding
    fun complete() {
        viewModelScope.launch {
            val now = Clock.System.now()
            val today = LocalDate.today()
            val currencyCode = _selectedCurrencyCode.value
            val incomeMinor = CurrencyFormatter.parseToMinorUnits(_incomeAmount.value, 2) ?: 0L

            val income = Income(
                amountMinor = incomeMinor,
                currencyCode = currencyCode,
                label = _incomeLabel.value,
                isRecurring = true,
                recurrenceRule = "MONTHLY",
                startDate = today,
                createdAt = now,
            )

            val recurringExpenses = _fixedExpenses.value
                .filter { it.amountMinor > 0 }
                .map { expense ->
                    RecurringExpense(
                        amountMinor = expense.amountMinor,
                        currencyCode = currencyCode,
                        categoryId = expense.categoryId,
                        label = expense.label,
                        recurrenceRule = "MONTHLY",
                        nextDate = today,
                        isActive = true,
                        createdAt = now,
                    )
                }

            val savingsBuckets = _savingsGoals.value.mapIndexed { index, goal ->
                SavingsBucket(
                    label = goal.label,
                    type = goal.type,
                    currencyCode = currencyCode,
                    monthlyContributionMinor = goal.monthlyContributionMinor,
                    targetAmountMinor = goal.targetAmountMinor,
                    deadline = null,
                    currentBalanceMinor = 0L,
                    isActive = true,
                    sortOrder = index,
                )
            }

            completeOnboardingUseCase(
                currencyCode = currencyCode,
                incomeDay = _incomeDay.value,
                income = income,
                recurringExpenses = recurringExpenses,
                savingsBuckets = savingsBuckets,
            )

            _events.emit(OnboardingEvent.Completed)
        }
    }
}
