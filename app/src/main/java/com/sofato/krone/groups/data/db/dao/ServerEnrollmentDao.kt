package com.sofato.krone.groups.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sofato.krone.groups.data.db.entity.ServerEnrollmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerEnrollmentDao {

    @Query("SELECT * FROM server_enrollment WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = ServerEnrollmentEntity.SINGLE_ROW_ID): ServerEnrollmentEntity?

    @Query("SELECT * FROM server_enrollment WHERE id = :id LIMIT 1")
    fun observe(id: Int = ServerEnrollmentEntity.SINGLE_ROW_ID): Flow<ServerEnrollmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ServerEnrollmentEntity)

    @Query("DELETE FROM server_enrollment")
    suspend fun clear()
}
