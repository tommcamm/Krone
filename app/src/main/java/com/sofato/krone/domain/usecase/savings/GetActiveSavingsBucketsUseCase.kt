package com.sofato.krone.domain.usecase.savings

import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.repository.SavingsBucketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveSavingsBucketsUseCase @Inject constructor(
    private val savingsBucketRepository: SavingsBucketRepository,
) {
    operator fun invoke(): Flow<List<SavingsBucket>> =
        savingsBucketRepository.getActiveBuckets()
}
