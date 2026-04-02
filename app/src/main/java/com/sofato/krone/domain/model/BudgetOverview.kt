package com.sofato.krone.domain.model

data class BudgetOverview(
    val period: BudgetPeriod,
    val totalIncomeMinor: Long,
    val totalFixedMinor: Long,
    val totalSavingsMinor: Long,
    val discretionaryMinor: Long,
    val spentMinor: Long,
    val categoryBreakdown: List<CategorySpend>,
    val currencyCode: String,
    val totalAllocatedMinor: Long = 0L,
    val unallocatedDiscretionaryMinor: Long = discretionaryMinor,
)

data class CategorySpend(
    val category: Category,
    val allocatedMinor: Long,
    val spentMinor: Long,
)
