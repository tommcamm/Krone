package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sofato.krone.data.db.entity.MlModelMetadataEntity

@Dao
interface MlModelMetadataDao {

    @Query("SELECT * FROM ml_model_metadata WHERE modelType = :modelType ORDER BY version DESC LIMIT 1")
    suspend fun getLatestModel(modelType: String): MlModelMetadataEntity?

    @Insert
    suspend fun insert(entity: MlModelMetadataEntity): Long
}
