package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "exchange_rate",
    indices = [Index(value = ["baseCode", "targetCode", "fetchedAt"])],
)
data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val baseCode: String,
    val targetCode: String,
    val rate: Double,
    val fetchedAt: Instant,
    val source: String,
)
