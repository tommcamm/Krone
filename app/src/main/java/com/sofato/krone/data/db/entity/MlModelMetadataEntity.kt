package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "ml_model_metadata")
data class MlModelMetadataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modelType: String,
    val version: Int,
    val trainedAt: Instant?,
    val sampleCount: Int,
    val accuracyEstimate: Float?,
)
