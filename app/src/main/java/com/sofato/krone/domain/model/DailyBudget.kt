package com.sofato.krone.domain.model

data class DailyBudget(
    val dailyAmountMinor: Long,
    val totalIncomeMinor: Long,
    val totalFixedMinor: Long,
    val totalSavingsMinor: Long,
    val spentSoFarMinor: Long,
    val remainingDays: Int,
    val discretionaryMinor: Long,
    val currencyCode: String,
)
