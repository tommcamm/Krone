package com.sofato.krone.data.db.dao.projections

data class CurrencyTotal(
    val currencyCode: String,
    val originalTotalMinor: Long,
    val homeTotalMinor: Long,
)
