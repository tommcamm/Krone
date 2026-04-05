package com.sofato.krone.domain.usecase.budget

import com.sofato.krone.domain.model.BudgetAllocation
import com.sofato.krone.domain.repository.BudgetAllocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import javax.inject.Inject

class GetOrCopyForwardAllocationsUseCase @Inject constructor(
    private val budgetAllocationRepository: BudgetAllocationRepository,
) {
    operator fun invoke(month: String): Flow<List<BudgetAllocation>> = flow {
        val current = budgetAllocationRepository.getAllocationsForMonth(month).first()
        if (current.isEmpty()) {
            val previousMonth = computePreviousMonth(month)
            val previous = budgetAllocationRepository.getAllocationsForMonth(previousMonth).first()
            if (previous.isNotEmpty()) {
                val copied = previous.map { it.copy(id = 0, month = month) }
                budgetAllocationRepository.upsertAll(copied)
            }
        }
        emitAll(budgetAllocationRepository.getAllocationsForMonth(month))
    }

    companion object {
        fun computePreviousMonth(month: String): String {
            val date = LocalDate.parse("$month-01")
            val prev = if (date.month.number == 1) {
                LocalDate(date.year - 1, 12, 1)
            } else {
                LocalDate(date.year, date.month.number - 1, 1)
            }
            return "${prev.year}-${prev.month.number.toString().padStart(2, '0')}"
        }

        fun formatMonth(date: LocalDate): String =
            "${date.year}-${date.month.number.toString().padStart(2, '0')}"
    }
}
