package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.RecurringExpenseDao
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.db.entity.toEntity
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class RecurringExpenseRepositoryImpl @Inject constructor(
    private val recurringExpenseDao: RecurringExpenseDao,
) : RecurringExpenseRepository {

    override fun getActiveRecurring(): Flow<List<RecurringExpense>> =
        recurringExpenseDao.getActiveRecurring().map { list -> list.map { it.toDomain() } }

    override fun getTotalActiveRecurringMinor(): Flow<Long?> =
        recurringExpenseDao.getTotalActiveRecurringMinor()

    override suspend fun getById(id: Long): RecurringExpense? =
        recurringExpenseDao.getById(id)?.toDomain()

    override suspend fun addRecurringExpense(expense: RecurringExpense): Long =
        recurringExpenseDao.insert(expense.toEntity())

    override suspend fun updateRecurringExpense(expense: RecurringExpense) {
        recurringExpenseDao.update(expense.toEntity())
    }

    override suspend fun deactivate(id: Long) {
        recurringExpenseDao.deactivate(id)
    }

    override suspend fun getDueRecurring(date: LocalDate): List<RecurringExpense> =
        recurringExpenseDao.getDueRecurring(date).map { it.toDomain() }

    override suspend fun updateNextDate(id: Long, nextDate: LocalDate) {
        recurringExpenseDao.updateNextDate(id, nextDate)
    }
}
