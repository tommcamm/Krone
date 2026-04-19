package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.CategoryTotal
import com.sofato.krone.domain.model.CurrencyTotal
import com.sofato.krone.domain.model.DailyTotal
import com.sofato.krone.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ExpenseRepository {
    fun getExpensesByDate(date: LocalDate): Flow<List<Expense>>
    fun getExpensesBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>>
    fun getRecentExpenses(limit: Int): Flow<List<Expense>>
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun addExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: Long)
    suspend fun reInsertExpense(expense: Expense)
    suspend fun deleteRecurringInstances(recurringExpenseId: Long, startDate: LocalDate, endDate: LocalDate)
    fun getTotalHomeAmountBetween(startDate: LocalDate, endDate: LocalDate): Flow<Long?>
    fun getTotalDiscretionaryAmountBetween(startDate: LocalDate, endDate: LocalDate): Flow<Long?>
    fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTotal>>
    fun getCategoryTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<CategoryTotal>>
    fun getCurrencyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<CurrencyTotal>>
}
