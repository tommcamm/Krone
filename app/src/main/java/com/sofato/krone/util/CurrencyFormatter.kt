package com.sofato.krone.util

import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.SymbolPosition
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

object CurrencyFormatter {

    fun format(amountMinor: Long, currency: Currency): String {
        val divisor = 10.0.pow(currency.decimalPlaces)
        val amount = amountMinor / divisor
        val locale = getLocaleForCurrency(currency.code)
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = java.util.Currency.getInstance(currency.code)
            maximumFractionDigits = currency.decimalPlaces
            minimumFractionDigits = currency.decimalPlaces
        }
        return formatter.format(amount)
    }

    fun formatPlain(amountMinor: Long, decimalPlaces: Int): String {
        val divisor = 10.0.pow(decimalPlaces)
        val amount = amountMinor / divisor
        return String.format(Locale.US, "%.${decimalPlaces}f", amount)
    }

    fun formatDisplay(amountMinor: Long, currency: Currency): String {
        val divisor = 10.0.pow(currency.decimalPlaces)
        val amount = amountMinor / divisor
        val locale = getLocaleForCurrency(currency.code)
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = currency.decimalPlaces
            minimumFractionDigits = currency.decimalPlaces
        }
        val formatted = formatter.format(amount)
        return when (currency.symbolPosition) {
            SymbolPosition.BEFORE -> "${currency.symbol}$formatted"
            SymbolPosition.AFTER -> "$formatted ${currency.symbol}"
        }
    }

    fun parseToMinorUnits(input: String, decimalPlaces: Int): Long? {
        val cleaned = input.replace(",", ".").replace(" ", "").replace("\u00A0", "")
        val parsed = cleaned.toDoubleOrNull() ?: return null
        return (parsed * 10.0.pow(decimalPlaces)).toLong()
    }

    private fun getLocaleForCurrency(code: String): Locale = when (code) {
        "DKK" -> Locale.of("da", "DK")
        "EUR" -> Locale.of("de", "DE")
        "USD" -> Locale.US
        "NZD" -> Locale.of("en", "NZ")
        else -> Locale.getDefault()
    }
}
