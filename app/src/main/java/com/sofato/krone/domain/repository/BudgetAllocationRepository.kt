package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.BudgetAllocation
import kotlinx.coroutines.flow.Flow

interface BudgetAllocationRepository {
    fun getAllocationsForMonth(month: String): Flow<List<BudgetAllocation>>
    suspend fun addAllocation(allocation: BudgetAllocation): Long
    suspend fun updateAllocation(allocation: BudgetAllocation)
    suspend fun deleteAllocation(id: Long)
    suspend fun upsertAll(allocations: List<BudgetAllocation>)
}
