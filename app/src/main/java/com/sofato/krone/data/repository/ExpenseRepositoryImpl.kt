package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.CategoryDao
import com.sofato.krone.data.db.dao.CurrencyDao
import com.sofato.krone.data.db.dao.ExpenseDao
import com.sofato.krone.data.db.entity.ExpenseEntity
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.domain.model.CategoryTotal
import com.sofato.krone.domain.model.CurrencyTotal
import com.sofato.krone.domain.model.DailyTotal
import com.sofato.krone.domain.model.Expense
import com.sofato.krone.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val currencyDao: CurrencyDao,
) : ExpenseRepository {

    override fun getExpensesByDate(date: LocalDate): Flow<List<Expense>> =
        expenseDao.getExpensesByDate(date).map { it.toDomainList() }

    override fun getExpensesBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<Expense>> =
        expenseDao.getExpensesBetween(startDate, endDate).map { it.toDomainList() }

    override fun getRecentExpenses(limit: Int): Flow<List<Expense>> =
        expenseDao.getRecentExpenses(limit).map { it.toDomainList() }

    override fun getAllExpenses(): Flow<List<Expense>> =
        expenseDao.getAllExpenses().map { it.toDomainList() }

    override suspend fun getExpenseById(id: Long): Expense? {
        val entity = expenseDao.getExpenseById(id) ?: return null
        return listOf(entity).toDomainList().firstOrNull()
    }

    override suspend fun addExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    override suspend fun reInsertExpense(expense: Expense) {
        expenseDao.reInsertExpense(expense.toEntity())
    }

    override suspend fun deleteRecurringInstances(recurringExpenseId: Long, startDate: LocalDate, endDate: LocalDate) {
        expenseDao.deleteRecurringInstances(recurringExpenseId, startDate, endDate)
    }

    override fun getTotalHomeAmountBetween(startDate: LocalDate, endDate: LocalDate): Flow<Long?> =
        expenseDao.getTotalHomeAmountBetween(startDate, endDate)

    override fun getTotalDiscretionaryAmountBetween(startDate: LocalDate, endDate: LocalDate): Flow<Long?> =
        expenseDao.getTotalDiscretionaryAmountBetween(startDate, endDate)

    override fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTotal>> =
        expenseDao.getDailyTotals(startDate, endDate)

    override fun getCategoryTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<CategoryTotal>> =
        expenseDao.getCategoryTotals(startDate, endDate)

    override fun getCurrencyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<CurrencyTotal>> =
        expenseDao.getCurrencyTotals(startDate, endDate)

    /**
     * Batch-converts a list of ExpenseEntities to domain models by pre-fetching all needed
     * categories and currencies in two queries, instead of querying per entity.
     */
    private suspend fun List<ExpenseEntity>.toDomainList(): List<Expense> {
        if (isEmpty()) return emptyList()

        val categoryIds = map { it.categoryId }.toSet()
        val currencyCodes = map { it.currencyCode }.toSet()

        val categories = categoryIds.mapNotNull { categoryDao.getCategoryById(it) }
            .associate { it.id to it.toDomain() }
        val currencies = currencyCodes.mapNotNull { currencyDao.getCurrencyByCode(it) }
            .associate { it.code to it.toDomain() }

        return mapNotNull { entity ->
            val category = categories[entity.categoryId] ?: return@mapNotNull null
            val currency = currencies[entity.currencyCode] ?: return@mapNotNull null
            Expense(
                id = entity.id,
                amount = entity.amountMinor,
                currency = currency,
                homeAmount = entity.homeAmountMinor,
                exchangeRateUsed = entity.exchangeRateUsed,
                category = category,
                note = entity.note,
                date = entity.date,
                createdAt = entity.createdAt,
                isRecurringInstance = entity.isRecurringInstance,
                recurringExpenseId = entity.recurringExpenseId,
            )
        }
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
