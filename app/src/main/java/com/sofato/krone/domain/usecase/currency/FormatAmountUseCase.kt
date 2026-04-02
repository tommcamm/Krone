package com.sofato.krone.domain.usecase.currency

import com.sofato.krone.domain.model.Currency
import com.sofato.krone.util.CurrencyFormatter
import javax.inject.Inject

class FormatAmountUseCase @Inject constructor() {
    operator fun invoke(amountMinor: Long, currency: Currency): String =
        CurrencyFormatter.formatDisplay(amountMinor, currency)
}
