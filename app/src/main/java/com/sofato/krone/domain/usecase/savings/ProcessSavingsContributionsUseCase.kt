package com.sofato.krone.domain.usecase.savings

import com.sofato.krone.domain.model.SavingsContribution
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.SavingsContributionRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ProcessSavingsContributionsUseCase @Inject constructor(
    private val savingsBucketRepository: SavingsBucketRepository,
    private val savingsContributionRepository: SavingsContributionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke() {
        val today = LocalDate.today()
        val incomeDay = userPreferencesRepository.incomeDay.first()
        if (today.dayOfMonth != incomeDay) return

        val buckets = savingsBucketRepository.getActiveBuckets().first()
        for (bucket in buckets) {
            // Check if already auto-posted today (idempotent)
            val existing = savingsContributionRepository.getAutoPostedForDate(bucket.id, today)
            if (existing != null) continue

            val contribution = SavingsContribution(
                bucketId = bucket.id,
                amountMinor = bucket.monthlyContributionMinor,
                date = today,
                isAutoPosted = true,
                isSkipped = false,
            )
            savingsContributionRepository.addContribution(contribution)

            // Update bucket balance
            savingsBucketRepository.updateBalance(
                id = bucket.id,
                balance = bucket.currentBalanceMinor + bucket.monthlyContributionMinor,
                updatedAt = Clock.System.now(),
            )
        }
    }
}
