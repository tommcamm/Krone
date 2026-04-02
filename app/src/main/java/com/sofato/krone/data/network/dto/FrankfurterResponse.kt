package com.sofato.krone.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class FrankfurterResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
)
