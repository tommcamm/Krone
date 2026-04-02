package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.BudgetAllocationDao
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.db.entity.toEntity
import com.sofato.krone.domain.model.BudgetAllocation
import com.sofato.krone.domain.repository.BudgetAllocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetAllocationRepositoryImpl @Inject constructor(
    private val budgetAllocationDao: BudgetAllocationDao,
) : BudgetAllocationRepository {

    override fun getAllocationsForMonth(month: String): Flow<List<BudgetAllocation>> =
        budgetAllocationDao.getAllocationsForMonth(month).map { list -> list.map { it.toDomain() } }

    override suspend fun addAllocation(allocation: BudgetAllocation): Long =
        budgetAllocationDao.insert(allocation.toEntity())

    override suspend fun updateAllocation(allocation: BudgetAllocation) {
        budgetAllocationDao.update(allocation.toEntity())
    }

    override suspend fun deleteAllocation(id: Long) {
        budgetAllocationDao.deleteById(id)
    }

    override suspend fun upsertAll(allocations: List<BudgetAllocation>) {
        budgetAllocationDao.upsertAll(allocations.map { it.toEntity() })
    }
}
