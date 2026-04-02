package com.sofato.krone.domain.usecase.budget

import com.sofato.krone.domain.model.BudgetPeriod
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.util.calculateBudgetPeriod
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class CalculateBudgetPeriodUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(referenceDate: LocalDate = LocalDate.today()): BudgetPeriod {
        val incomeDay = userPreferencesRepository.incomeDay.first()
        return calculateBudgetPeriod(incomeDay, referenceDate)
    }
}
