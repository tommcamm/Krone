package com.sofato.krone.domain.usecase.savings

import com.sofato.krone.domain.repository.SavingsBucketRepository
import javax.inject.Inject

class DeactivateSavingsBucketUseCase @Inject constructor(
    private val savingsBucketRepository: SavingsBucketRepository,
) {
    suspend operator fun invoke(id: Long) {
        savingsBucketRepository.deactivateBucket(id)
    }
}
