package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sofato.krone.data.db.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rate WHERE baseCode = :base AND targetCode = :target ORDER BY fetchedAt DESC LIMIT 1")
    suspend fun getLatestRate(base: String, target: String): ExchangeRateEntity?

    @Query("SELECT * FROM exchange_rate WHERE baseCode = :base AND targetCode = :target ORDER BY fetchedAt DESC LIMIT 1")
    fun observeLatestRate(base: String, target: String): Flow<ExchangeRateEntity?>

    @Query("SELECT MAX(fetchedAt) FROM exchange_rate")
    suspend fun getLatestFetchTime(): Instant?

    @Insert
    suspend fun insertRate(rate: ExchangeRateEntity): Long

    @Insert
    suspend fun insertRates(rates: List<ExchangeRateEntity>)
}
