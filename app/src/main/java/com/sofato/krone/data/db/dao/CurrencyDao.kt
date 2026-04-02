package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sofato.krone.data.db.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currency ORDER BY sortOrder ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currency WHERE isEnabled = 1 ORDER BY sortOrder ASC")
    fun getEnabledCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currency WHERE code = :code")
    suspend fun getCurrencyByCode(code: String): CurrencyEntity?

    @Upsert
    suspend fun upsertCurrency(currency: CurrencyEntity)

    @Upsert
    suspend fun upsertCurrencies(currencies: List<CurrencyEntity>)

    @Query("UPDATE currency SET isEnabled = :isEnabled WHERE code = :code")
    suspend fun setEnabled(code: String, isEnabled: Boolean)
}
