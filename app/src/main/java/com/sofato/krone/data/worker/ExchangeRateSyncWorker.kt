package com.sofato.krone.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sofato.krone.domain.repository.ExchangeRateRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

@HiltWorker
class ExchangeRateSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val exchangeRateRepository: ExchangeRateRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return exchangeRateRepository.refreshRates().fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                // Only retry on transient network errors; fail immediately on parse/DB errors.
                if (error is IOException) Result.retry() else Result.failure()
            },
        )
    }

    companion object {
        const val WORK_NAME = "exchange_rate_sync"
    }
}
