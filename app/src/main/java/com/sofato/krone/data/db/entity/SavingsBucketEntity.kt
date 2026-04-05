package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sofato.krone.domain.model.SavingsBucketType
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "savings_bucket",
    foreignKeys = [
        ForeignKey(
            entity = CurrencyEntity::class,
            parentColumns = ["code"],
            childColumns = ["currencyCode"],
        ),
    ],
    indices = [Index("currencyCode")],
)
data class SavingsBucketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val type: SavingsBucketType,
    val currencyCode: String,
    val monthlyContributionMinor: Long,
    val targetAmountMinor: Long?,
    val deadline: LocalDate?,
    val currentBalanceMinor: Long,
    val balanceUpdatedAt: Instant?,
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: Instant,
)
