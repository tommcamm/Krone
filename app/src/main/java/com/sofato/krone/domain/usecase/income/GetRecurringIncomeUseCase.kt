package com.sofato.krone.domain.usecase.income

import com.sofato.krone.domain.model.Income
import com.sofato.krone.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecurringIncomeUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository,
) {
    operator fun invoke(): Flow<List<Income>> =
        incomeRepository.getRecurringIncome()
}
