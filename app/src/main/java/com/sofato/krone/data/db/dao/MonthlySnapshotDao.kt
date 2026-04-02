package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sofato.krone.data.db.entity.MonthlySnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlySnapshotDao {

    @Query("SELECT * FROM monthly_snapshot ORDER BY month DESC")
    fun getAllSnapshots(): Flow<List<MonthlySnapshotEntity>>

    @Query("SELECT * FROM monthly_snapshot WHERE month = :month")
    suspend fun getSnapshotForMonth(month: String): MonthlySnapshotEntity?

    @Insert
    suspend fun insert(entity: MonthlySnapshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MonthlySnapshotEntity): Long
}
