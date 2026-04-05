package com.sofato.krone.domain.usecase.insights

import com.sofato.krone.domain.model.CategoryAmount
import com.sofato.krone.domain.model.MonthCategoryBreakdown
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.util.endOfMonth
import com.sofato.krone.util.startOfMonth
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject

class GetCategoryTrendUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(monthsBack: Int = 6): Flow<List<MonthCategoryBreakdown>> {
        val today = LocalDate.today()
        val months = (0 until monthsBack).map { offset ->
            val d = if (offset == 0) today else today.startOfMonth().minus(offset.toLong(), DateTimeUnit.MONTH)
            val start = d.startOfMonth()
            val end = if (offset == 0) today else start.endOfMonth()
            start to end
        }.reversed()

        val categoryTotalFlows = months.map { (start, end) ->
            expenseRepository.getCategoryTotals(start, end)
        }

        return combine(
            combine(categoryTotalFlows) { it.toList() },
            categoryRepository.getActiveCategories(),
        ) { allTotals, categories ->
            val categoryMap = categories.associate { it.id to it }

            months.mapIndexed { index, (start, _) ->
                val monthLabel = "${start.year}-${start.monthNumber.toString().padStart(2, '0')}"
                val totals = allTotals[index]
                val categoryAmounts = totals.mapNotNull { ct ->
                    val cat = categoryMap[ct.categoryId] ?: return@mapNotNull null
                    CategoryAmount(
                        categoryName = cat.name,
                        colorHex = cat.colorHex,
                        amountMinor = ct.totalMinor,
                    )
                }.sortedByDescending { it.amountMinor }

                MonthCategoryBreakdown(month = monthLabel, categories = categoryAmounts)
            }
        }
    }
}
