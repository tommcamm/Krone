package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sofato.krone.data.db.entity.SavingsContributionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface SavingsContributionDao {

    @Query("SELECT * FROM savings_contribution WHERE bucketId = :bucketId ORDER BY date DESC")
    fun getContributionsForBucket(bucketId: Long): Flow<List<SavingsContributionEntity>>

    @Query("SELECT * FROM savings_contribution WHERE bucketId = :bucketId AND date BETWEEN :start AND :end")
    fun getContributionsForBucketBetween(bucketId: Long, start: LocalDate, end: LocalDate): Flow<List<SavingsContributionEntity>>

    @Query("SELECT SUM(amountMinor) FROM savings_contribution WHERE date BETWEEN :start AND :end AND isSkipped = 0")
    fun getTotalContributionsBetween(start: LocalDate, end: LocalDate): Flow<Long?>

    @Query("SELECT * FROM savings_contribution WHERE bucketId = :bucketId AND date = :date AND isAutoPosted = 1 LIMIT 1")
    suspend fun getAutoPostedForDate(bucketId: Long, date: LocalDate): SavingsContributionEntity?

    @Insert
    suspend fun insert(entity: SavingsContributionEntity): Long

    @Update
    suspend fun update(entity: SavingsContributionEntity)
}
