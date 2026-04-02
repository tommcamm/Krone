package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.Income
import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    fun getAllIncome(): Flow<List<Income>>
    fun getRecurringIncome(): Flow<List<Income>>
    fun getTotalRecurringIncomeMinor(): Flow<Long?>
    suspend fun getIncomeById(id: Long): Income?
    suspend fun addIncome(income: Income): Long
    suspend fun updateIncome(income: Income)
    suspend fun deleteIncome(id: Long)
}
