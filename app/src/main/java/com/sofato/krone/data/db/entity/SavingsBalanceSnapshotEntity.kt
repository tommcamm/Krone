package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(
    tableName = "savings_balance_snapshot",
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
data class SavingsBalanceSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bucketId: Long,
    val balanceMinor: Long,
    val recordedAt: Instant,
)
