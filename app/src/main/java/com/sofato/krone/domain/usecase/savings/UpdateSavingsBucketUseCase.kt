package com.sofato.krone.domain.usecase.savings

import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.repository.SavingsBucketRepository
import javax.inject.Inject

class UpdateSavingsBucketUseCase @Inject constructor(
    private val savingsBucketRepository: SavingsBucketRepository,
) {
    suspend operator fun invoke(bucket: SavingsBucket) {
        savingsBucketRepository.updateBucket(bucket)
    }
}
