package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.CurrencyDao
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val currencyDao: CurrencyDao,
) : CurrencyRepository {

    override fun getEnabledCurrencies(): Flow<List<Currency>> =
        currencyDao.getEnabledCurrencies().map { list -> list.map { it.toDomain() } }

    override fun getAllCurrencies(): Flow<List<Currency>> =
        currencyDao.getAllCurrencies().map { list -> list.map { it.toDomain() } }

    override suspend fun getCurrencyByCode(code: String): Currency? =
        currencyDao.getCurrencyByCode(code)?.toDomain()

    override suspend fun setEnabled(code: String, enabled: Boolean) {
        currencyDao.setEnabled(code, enabled)
    }
}
