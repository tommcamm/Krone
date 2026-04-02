package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sofato.krone.domain.model.SymbolPosition

@Entity(tableName = "currency")
data class CurrencyEntity(
    @PrimaryKey val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: SymbolPosition,
    val isEnabled: Boolean,
    val sortOrder: Int,
)
