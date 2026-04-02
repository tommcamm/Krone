package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.CategoryDao
import com.sofato.krone.data.db.dao.CurrencyDao
import com.sofato.krone.data.db.dao.ExpenseDao
import com.sofato.krone.data.db.entity.ExpenseEntity
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.model.SymbolPosition
import com.sofato.krone.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val currencyDao: CurrencyDao,
) : ExpenseRepository {

    override fun getExpensesByDate(date: LocalDate): Flow<List<Expense>> =
        expenseDao.getExpensesByDate(date).map { entities -> entities.mapNotNull { it.toDomain() } }

    override fun getExpensesBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> =
        expenseDao.getExpensesBetween(startDate, endDate).map { entities -> entities.mapNotNull { it.toDomain() } }

    override fun getRecentExpenses(limit: Int): Flow<List<Expense>> =
        expenseDao.getRecentExpenses(limit).map { entities -> entities.mapNotNull { it.toDomain() } }

    override suspend fun getExpenseById(id: Long): Expense? =
        expenseDao.getExpenseById(id)?.toDomain()

    override suspend fun addExpense(expense: Expense): Long {
        val entity = expense.toEntity()
        return expenseDao.insertExpense(entity)
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    override fun getTotalHomeAmountBetween(startDate: LocalDate, endDate: LocalDate): Flow<Long?> =
        expenseDao.getTotalHomeAmountBetween(startDate, endDate)

    private suspend fun ExpenseEntity.toDomain(): Expense? {
        val category = categoryDao.getCategoryById(categoryId)?.toDomain() ?: return null
        val currency = currencyDao.getCurrencyByCode(currencyCode)?.toDomain() ?: return null
        return Expense(
            id = id,
            amount = amountMinor,
            currency = currency,
            homeAmount = homeAmountMinor,
            exchangeRateUsed = exchangeRateUsed,
            category = category,
            note = note,
            date = date,
            createdAt = createdAt,
            isRecurringInstance = isRecurringInstance,
            recurringExpenseId = recurringExpenseId,
        )
    }

    private fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
        id = id,
        amountMinor = amount,
        currencyCode = currency.code,
        homeAmountMinor = homeAmount,
        exchangeRateUsed = exchangeRateUsed,
        categoryId = category.id,
        note = note,
        date = date,
        createdAt = if (id == 0L) Clock.System.now() else createdAt,
        isRecurringInstance = isRecurringInstance,
        recurringExpenseId = recurringExpenseId,
        mlCategorySuggestion = null,
        mlSuggestionAccepted = null,
    )
}
