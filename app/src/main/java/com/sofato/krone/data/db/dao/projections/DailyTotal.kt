package com.sofato.krone.data.db.dao.projections

import kotlinx.datetime.LocalDate

data class DailyTotal(
    val date: LocalDate,
    val totalMinor: Long,
)
