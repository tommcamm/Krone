package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "exchange_rate",
    indices = [Index(value = ["baseCode", "targetCode", "rateDate"], unique = true)],
)
data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val baseCode: String,
    val targetCode: String,
    val rate: Double,
    val rateDate: LocalDate,
    val fetchedAt: Instant,
    val source: String,
)
