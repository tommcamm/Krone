package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sofato.krone.data.db.entity.SavingsBalanceSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsBalanceSnapshotDao {

    @Query("SELECT * FROM savings_balance_snapshot WHERE bucketId = :bucketId ORDER BY recordedAt DESC")
    fun getSnapshotsForBucket(bucketId: Long): Flow<List<SavingsBalanceSnapshotEntity>>

    @Query("SELECT * FROM savings_balance_snapshot WHERE bucketId = :bucketId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestForBucket(bucketId: Long): SavingsBalanceSnapshotEntity?

    @Insert
    suspend fun insert(entity: SavingsBalanceSnapshotEntity): Long
}
