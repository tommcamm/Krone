package com.sofato.krone.domain.usecase.currency

import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEnabledCurrenciesUseCase @Inject constructor(
    private val currencyRepository: CurrencyRepository,
) {
    operator fun invoke(): Flow<List<Currency>> =
        currencyRepository.getEnabledCurrencies()
}
