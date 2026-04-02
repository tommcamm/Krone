package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.SavingsContributionDao
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.db.entity.toEntity
import com.sofato.krone.domain.model.SavingsContribution
import com.sofato.krone.domain.repository.SavingsContributionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class SavingsContributionRepositoryImpl @Inject constructor(
    private val savingsContributionDao: SavingsContributionDao,
) : SavingsContributionRepository {

    override fun getContributionsForBucket(bucketId: Long): Flow<List<SavingsContribution>> =
        savingsContributionDao.getContributionsForBucket(bucketId).map { list -> list.map { it.toDomain() } }

    override fun getTotalContributionsBetween(start: LocalDate, end: LocalDate): Flow<Long?> =
        savingsContributionDao.getTotalContributionsBetween(start, end)

    override suspend fun addContribution(contribution: SavingsContribution): Long =
        savingsContributionDao.insert(contribution.toEntity())

    override suspend fun updateContribution(contribution: SavingsContribution) {
        savingsContributionDao.update(contribution.toEntity())
    }

    override suspend fun getAutoPostedForDate(bucketId: Long, date: LocalDate): SavingsContribution? =
        savingsContributionDao.getAutoPostedForDate(bucketId, date)?.toDomain()
}
