package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sofato.krone.data.db.dao.projections.CategoryTotal
import com.sofato.krone.data.db.dao.projections.CurrencyTotal
import com.sofato.krone.data.db.dao.projections.DailyTotal
import com.sofato.krone.data.db.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense WHERE date = :date ORDER BY createdAt DESC")
    fun getExpensesByDate(date: LocalDate): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, createdAt DESC")
    fun getExpensesBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expense WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT SUM(homeAmountMinor) FROM expense WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalHomeAmountBetween(startDate: LocalDate, endDate: LocalDate): Flow<Long?>

    @Query("SELECT SUM(homeAmountMinor) FROM expense WHERE date BETWEEN :startDate AND :endDate AND isRecurringInstance = 0")
    fun getTotalDiscretionaryAmountBetween(startDate: LocalDate, endDate: LocalDate): Flow<Long?>

    @Query("SELECT * FROM expense ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT COUNT(*) FROM expense")
    suspend fun getExpenseCount(): Int

    @Query(
        "SELECT date, SUM(homeAmountMinor) AS totalMinor " +
            "FROM expense WHERE date BETWEEN :startDate AND :endDate " +
            "GROUP BY date ORDER BY date ASC",
    )
    fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTotal>>

    @Query(
        "SELECT categoryId, SUM(homeAmountMinor) AS totalMinor " +
            "FROM expense WHERE date BETWEEN :startDate AND :endDate " +
            "GROUP BY categoryId ORDER BY totalMinor DESC",
    )
    fun getCategoryTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<CategoryTotal>>

    @Query(
        "SELECT currencyCode, SUM(amountMinor) AS originalTotalMinor, " +
            "SUM(homeAmountMinor) AS homeTotalMinor " +
            "FROM expense WHERE date BETWEEN :startDate AND :endDate " +
            "GROUP BY currencyCode ORDER BY homeTotalMinor DESC",
    )
    fun getCurrencyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<CurrencyTotal>>
}
