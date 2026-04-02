package com.sofato.krone.domain.usecase.savings

import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.model.SavingsContribution
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.SavingsContributionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class SavingsBucketDetail(
    val bucket: SavingsBucket,
    val contributions: List<SavingsContribution>,
)

class GetSavingsBucketDetailUseCase @Inject constructor(
    private val savingsBucketRepository: SavingsBucketRepository,
    private val savingsContributionRepository: SavingsContributionRepository,
) {
    suspend operator fun invoke(bucketId: Long): Flow<SavingsBucketDetail?> {
        val bucket = savingsBucketRepository.getById(bucketId) ?: return flowOf(null)
        return savingsContributionRepository.getContributionsForBucket(bucketId)
            .combine(flowOf(bucket)) { contributions, b ->
                SavingsBucketDetail(bucket = b, contributions = contributions)
            }
    }
}
