package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.SavingsBucket
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface SavingsBucketRepository {
    fun getActiveBuckets(): Flow<List<SavingsBucket>>
    fun getAllBuckets(): Flow<List<SavingsBucket>>
    fun getTotalMonthlyContributionsMinor(): Flow<Long?>
    suspend fun getById(id: Long): SavingsBucket?
    suspend fun addBucket(bucket: SavingsBucket): Long
    suspend fun updateBucket(bucket: SavingsBucket)
    suspend fun deactivateBucket(id: Long)
    suspend fun updateBalance(id: Long, balance: Long, updatedAt: Instant)
}
