package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sofato.krone.data.db.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Query("SELECT * FROM income ORDER BY createdAt DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE isRecurring = 1 ORDER BY createdAt DESC")
    fun getRecurringIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT SUM(amountMinor) FROM income WHERE isRecurring = 1")
    fun getTotalRecurringIncomeMinor(): Flow<Long?>

    @Query("SELECT * FROM income WHERE id = :id")
    suspend fun getIncomeById(id: Long): IncomeEntity?

    @Insert
    suspend fun insertIncome(income: IncomeEntity): Long

    @Update
    suspend fun update(entity: IncomeEntity)

    @Query("DELETE FROM income WHERE id = :id")
    suspend fun deleteById(id: Long)
}
