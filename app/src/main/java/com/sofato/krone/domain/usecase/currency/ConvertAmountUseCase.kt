package com.sofato.krone.domain.usecase.currency

import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
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
    suspend operator fun invoke(
        amountMinor: Long,
        fromCode: String,
        date: LocalDate = LocalDate.today(),
    ): ConversionResult? {
        val homeCode = userPreferencesRepository.homeCurrencyCode.first()
        if (fromCode == homeCode) {
            return ConversionResult(convertedAmountMinor = amountMinor, rateUsed = 1.0)
        }
        val rate = exchangeRateRepository.getRateForDate(fromCode, homeCode, date) ?: return null
        val converted = (amountMinor * rate.rate).roundToLong()
        return ConversionResult(convertedAmountMinor = converted, rateUsed = rate.rate)
    }
}
