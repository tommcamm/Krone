package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_snapshot",
    indices = [Index("month", unique = true)],
)
data class MonthlySnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: String,
    val totalIncomeMinor: Long,
    val totalFixedMinor: Long,
    val totalVariableMinor: Long,
    val totalSavingsMinor: Long,
    val homeCurrencyCode: String,
)
