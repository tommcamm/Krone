package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.RecurringExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface RecurringExpenseRepository {
    fun getActiveRecurring(): Flow<List<RecurringExpense>>
    fun getTotalActiveRecurringMinor(): Flow<Long?>
    suspend fun getById(id: Long): RecurringExpense?
    suspend fun addRecurringExpense(expense: RecurringExpense): Long
    suspend fun updateRecurringExpense(expense: RecurringExpense)
    suspend fun deactivate(id: Long)
    suspend fun getDueRecurring(date: LocalDate): List<RecurringExpense>
    suspend fun updateNextDate(id: Long, nextDate: LocalDate)
}
