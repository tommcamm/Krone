package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sofato.krone.data.db.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rate WHERE baseCode = :base AND targetCode = :target ORDER BY rateDate DESC LIMIT 1")
    suspend fun getLatestRate(base: String, target: String): ExchangeRateEntity?

    @Query("SELECT * FROM exchange_rate WHERE baseCode = :base AND targetCode = :target ORDER BY rateDate DESC LIMIT 1")
    fun observeLatestRate(base: String, target: String): Flow<ExchangeRateEntity?>

    @Query(
        "SELECT * FROM exchange_rate WHERE baseCode = :base AND targetCode = :target " +
            "AND rateDate <= :date ORDER BY rateDate DESC LIMIT 1"
    )
    suspend fun getRateOnOrBefore(base: String, target: String, date: LocalDate): ExchangeRateEntity?

    @Query(
        "SELECT * FROM exchange_rate WHERE baseCode = :base AND targetCode = :target " +
            "AND rateDate >= :date ORDER BY rateDate ASC LIMIT 1"
    )
    suspend fun getRateOnOrAfter(base: String, target: String, date: LocalDate): ExchangeRateEntity?

    @Query("SELECT MAX(fetchedAt) FROM exchange_rate")
    suspend fun getLatestFetchTime(): Instant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: ExchangeRateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)
}
