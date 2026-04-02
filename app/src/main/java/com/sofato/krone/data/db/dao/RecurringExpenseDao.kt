package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sofato.krone.data.db.entity.RecurringExpenseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface RecurringExpenseDao {

    @Query("SELECT * FROM recurring_expense WHERE isActive = 1 ORDER BY nextDate ASC")
    fun getActiveRecurring(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expense WHERE id = :id")
    suspend fun getById(id: Long): RecurringExpenseEntity?

    @Insert
    suspend fun insert(entity: RecurringExpenseEntity): Long

    @Update
    suspend fun update(entity: RecurringExpenseEntity)

    @Query("UPDATE recurring_expense SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("SELECT * FROM recurring_expense WHERE isActive = 1 AND nextDate <= :date ORDER BY nextDate ASC")
    suspend fun getDueRecurring(date: LocalDate): List<RecurringExpenseEntity>

    @Query("UPDATE recurring_expense SET nextDate = :nextDate WHERE id = :id")
    suspend fun updateNextDate(id: Long, nextDate: LocalDate)

    @Query("SELECT SUM(amountMinor) FROM recurring_expense WHERE isActive = 1")
    fun getTotalActiveRecurringMinor(): Flow<Long?>
}
