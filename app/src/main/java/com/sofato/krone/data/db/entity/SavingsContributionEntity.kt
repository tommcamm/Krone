package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "savings_contribution",
    foreignKeys = [
        ForeignKey(
            entity = SavingsBucketEntity::class,
            parentColumns = ["id"],
            childColumns = ["bucketId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bucketId")],
)
data class SavingsContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bucketId: Long,
    val amountMinor: Long,
    val date: LocalDate,
    val isAutoPosted: Boolean,
    val isSkipped: Boolean,
)
