package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.IncomeDao
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.db.entity.toEntity
import com.sofato.krone.domain.model.Income
import com.sofato.krone.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao,
) : IncomeRepository {

    override fun getAllIncome(): Flow<List<Income>> =
        incomeDao.getAllIncome().map { list -> list.map { it.toDomain() } }

    override fun getRecurringIncome(): Flow<List<Income>> =
        incomeDao.getRecurringIncome().map { list -> list.map { it.toDomain() } }

    override fun getTotalRecurringIncomeMinor(): Flow<Long?> =
        incomeDao.getTotalRecurringIncomeMinor()

    override suspend fun getIncomeById(id: Long): Income? =
        incomeDao.getIncomeById(id)?.toDomain()

    override suspend fun addIncome(income: Income): Long =
        incomeDao.insertIncome(income.toEntity())

    override suspend fun updateIncome(income: Income) {
        incomeDao.update(income.toEntity())
    }

    override suspend fun deleteIncome(id: Long) {
        incomeDao.deleteById(id)
    }
}
