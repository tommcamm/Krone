package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sofato.krone.data.db.entity.ExchangeRateEntity

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rate WHERE baseCode = :base AND targetCode = :target ORDER BY fetchedAt DESC LIMIT 1")
    suspend fun getLatestRate(base: String, target: String): ExchangeRateEntity?

    @Insert
    suspend fun insertRate(rate: ExchangeRateEntity): Long

    @Insert
    suspend fun insertRates(rates: List<ExchangeRateEntity>)
}
