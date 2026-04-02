package com.sofato.krone.domain.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: SymbolPosition,
    val isEnabled: Boolean,
    val sortOrder: Int,
)

enum class SymbolPosition { BEFORE, AFTER }
