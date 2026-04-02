package com.sofato.krone.domain.usecase.income

import com.sofato.krone.domain.model.Income
import com.sofato.krone.domain.repository.IncomeRepository
import javax.inject.Inject

class AddIncomeUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository,
) {
    suspend operator fun invoke(income: Income): Long =
        incomeRepository.addIncome(income)
}
