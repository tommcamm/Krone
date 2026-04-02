package com.sofato.krone.domain.model

data class BudgetAllocation(
    val id: Long = 0,
    val categoryId: Long,
    val month: String,
    val allocatedAmountMinor: Long,
    val currencyCode: String,
)
