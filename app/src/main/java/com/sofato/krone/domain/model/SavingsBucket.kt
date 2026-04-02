package com.sofato.krone.domain.model

import kotlinx.datetime.LocalDate

data class SavingsBucket(
    val id: Long = 0,
    val label: String,
    val type: SavingsBucketType,
    val currencyCode: String,
    val monthlyContributionMinor: Long,
    val targetAmountMinor: Long?,
    val deadline: LocalDate?,
    val currentBalanceMinor: Long,
    val isActive: Boolean,
    val sortOrder: Int,
)

enum class SavingsBucketType {
    EMERGENCY_FUND, INVESTMENT, GOAL, RECURRING, ASK, PENSION, FERIEPENGE
}
