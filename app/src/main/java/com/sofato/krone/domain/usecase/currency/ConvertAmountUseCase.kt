package com.sofato.krone.domain.usecase.currency

import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.roundToLong

data class ConversionResult(
    val convertedAmountMinor: Long,
    val rateUsed: Double,
)

class ConvertAmountUseCase @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(amountMinor: Long, fromCode: String): ConversionResult? {
        val homeCode = userPreferencesRepository.homeCurrencyCode.first()
        if (fromCode == homeCode) {
            return ConversionResult(convertedAmountMinor = amountMinor, rateUsed = 1.0)
        }
        val rate = exchangeRateRepository.getRate(fromCode, homeCode) ?: return null
        val converted = (amountMinor * rate.rate).roundToLong()
        return ConversionResult(convertedAmountMinor = converted, rateUsed = rate.rate)
    }
}
