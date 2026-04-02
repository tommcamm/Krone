package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getEnabledCurrencies(): Flow<List<Currency>>
    fun getAllCurrencies(): Flow<List<Currency>>
    suspend fun getCurrencyByCode(code: String): Currency?
    suspend fun setEnabled(code: String, enabled: Boolean)
}
