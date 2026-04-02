package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_snapshot")
data class MonthlySnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: String,
    val totalIncomeMinor: Long,
    val totalFixedMinor: Long,
    val totalVariableMinor: Long,
    val totalSavingsMinor: Long,
    val homeCurrencyCode: String,
)
