package com.sofato.krone.domain.usecase.onboarding

import com.sofato.krone.domain.model.Income
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.repository.IncomeRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val incomeRepository: IncomeRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val savingsBucketRepository: SavingsBucketRepository,
) {
    suspend operator fun invoke(
        currencyCode: String,
        incomeDay: Int,
        income: Income,
        recurringExpenses: List<RecurringExpense>,
        savingsBuckets: List<SavingsBucket>,
    ) {
        userPreferencesRepository.setHomeCurrencyCode(currencyCode)
        userPreferencesRepository.setIncomeDay(incomeDay)
        incomeRepository.addIncome(income)
        for (expense in recurringExpenses) {
            recurringExpenseRepository.addRecurringExpense(expense)
        }
        for (bucket in savingsBuckets) {
            savingsBucketRepository.addBucket(bucket)
        }
        userPreferencesRepository.setHasCompletedOnboarding(true)
    }
}
