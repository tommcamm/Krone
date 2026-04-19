package com.sofato.krone.domain.usecase.currency

import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.util.convertMinor
import com.sofato.krone.util.today
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class ConversionResult(
    val convertedAmountMinor: Long,
    val rateUsed: Double,
)

class ConvertAmountUseCase @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val currencyRepository: CurrencyRepository,
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
        val fromCurrency = currencyRepository.getCurrencyByCode(fromCode) ?: return null
        val homeCurrency = currencyRepository.getCurrencyByCode(homeCode) ?: return null
        val converted = convertMinor(
            amountMinor = amountMinor,
            rate = rate.rate,
            fromDecimals = fromCurrency.decimalPlaces,
            toDecimals = homeCurrency.decimalPlaces,
        )
        return ConversionResult(convertedAmountMinor = converted, rateUsed = rate.rate)
    }
}
