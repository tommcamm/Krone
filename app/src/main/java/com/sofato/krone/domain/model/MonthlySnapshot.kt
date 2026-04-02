package com.sofato.krone.domain.model

data class MonthlySnapshot(
    val id: Long = 0,
    val month: String,
    val totalIncomeMinor: Long,
    val totalFixedMinor: Long,
    val totalVariableMinor: Long,
    val totalSavingsMinor: Long,
    val homeCurrencyCode: String,
)
