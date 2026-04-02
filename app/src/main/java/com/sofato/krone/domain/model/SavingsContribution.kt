package com.sofato.krone.domain.model

import kotlinx.datetime.LocalDate

data class SavingsContribution(
    val id: Long = 0,
    val bucketId: Long,
    val amountMinor: Long,
    val date: LocalDate,
    val isAutoPosted: Boolean,
    val isSkipped: Boolean,
)
