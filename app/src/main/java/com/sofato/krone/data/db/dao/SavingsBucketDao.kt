package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sofato.krone.data.db.entity.SavingsBucketEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface SavingsBucketDao {

    @Query("SELECT * FROM savings_bucket WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getActiveBuckets(): Flow<List<SavingsBucketEntity>>

    @Query("SELECT * FROM savings_bucket ORDER BY sortOrder ASC")
    fun getAllBuckets(): Flow<List<SavingsBucketEntity>>

    @Query("SELECT * FROM savings_bucket WHERE id = :id")
    suspend fun getById(id: Long): SavingsBucketEntity?

    @Insert
    suspend fun insert(entity: SavingsBucketEntity): Long

    @Update
    suspend fun update(entity: SavingsBucketEntity)

    @Query("UPDATE savings_bucket SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("SELECT SUM(monthlyContributionMinor) FROM savings_bucket WHERE isActive = 1")
    fun getTotalMonthlyContributionsMinor(): Flow<Long?>

    @Query("UPDATE savings_bucket SET currentBalanceMinor = :balance, balanceUpdatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Long, updatedAt: Instant)
}
