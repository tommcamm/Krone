package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.SavingsContribution
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface SavingsContributionRepository {
    fun getContributionsForBucket(bucketId: Long): Flow<List<SavingsContribution>>
    fun getTotalContributionsBetween(start: LocalDate, end: LocalDate): Flow<Long?>
    suspend fun addContribution(contribution: SavingsContribution): Long
    suspend fun updateContribution(contribution: SavingsContribution)
    suspend fun getAutoPostedForDate(bucketId: Long, date: LocalDate): SavingsContribution?
}
