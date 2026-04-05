package com.sofato.krone.domain.usecase.savings

import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.model.SavingsContribution
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.SavingsContributionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SavingsBucketDetail(
    val bucket: SavingsBucket,
    val contributions: List<SavingsContribution>,
)

class GetSavingsBucketDetailUseCase @Inject constructor(
    private val savingsBucketRepository: SavingsBucketRepository,
    private val savingsContributionRepository: SavingsContributionRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(bucketId: Long): Flow<SavingsBucketDetail?> = flow {
        emit(savingsBucketRepository.getById(bucketId))
    }.flatMapLatest { bucket ->
        if (bucket == null) {
            flowOf(null)
        } else {
            savingsContributionRepository.getContributionsForBucket(bucketId)
                .map { contributions -> SavingsBucketDetail(bucket = bucket, contributions = contributions) }
        }
    }
}
