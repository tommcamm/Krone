package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.SavingsBucketDao
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.db.entity.toEntity
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.repository.SavingsBucketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

class SavingsBucketRepositoryImpl @Inject constructor(
    private val savingsBucketDao: SavingsBucketDao,
) : SavingsBucketRepository {

    override fun getActiveBuckets(): Flow<List<SavingsBucket>> =
        savingsBucketDao.getActiveBuckets().map { list -> list.map { it.toDomain() } }

    override fun getAllBuckets(): Flow<List<SavingsBucket>> =
        savingsBucketDao.getAllBuckets().map { list -> list.map { it.toDomain() } }

    override fun getTotalMonthlyContributionsMinor(): Flow<Long?> =
        savingsBucketDao.getTotalMonthlyContributionsMinor()

    override suspend fun getById(id: Long): SavingsBucket? =
        savingsBucketDao.getById(id)?.toDomain()

    override suspend fun addBucket(bucket: SavingsBucket): Long =
        savingsBucketDao.insert(bucket.toEntity())

    override suspend fun updateBucket(bucket: SavingsBucket) {
        savingsBucketDao.update(bucket.toEntity())
    }

    override suspend fun deactivateBucket(id: Long) {
        savingsBucketDao.deactivate(id)
    }

    override suspend fun updateBalance(id: Long, balance: Long, updatedAt: Instant) {
        savingsBucketDao.updateBalance(id, balance, updatedAt)
    }
}
