package com.sofato.krone.groups.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sofato.krone.groups.data.db.entity.DeviceIdentityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceIdentityDao {

    @Query("SELECT * FROM device_identity WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = DeviceIdentityEntity.SINGLE_ROW_ID): DeviceIdentityEntity?

    @Query("SELECT * FROM device_identity WHERE id = :id LIMIT 1")
    fun observe(id: Int = DeviceIdentityEntity.SINGLE_ROW_ID): Flow<DeviceIdentityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DeviceIdentityEntity)

    @Query("DELETE FROM device_identity")
    suspend fun clear()
}
