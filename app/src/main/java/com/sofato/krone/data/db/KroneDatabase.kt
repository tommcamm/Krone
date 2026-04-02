package com.sofato.krone.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sofato.krone.data.db.converter.Converters
import com.sofato.krone.data.db.dao.BudgetAllocationDao
import com.sofato.krone.data.db.dao.CategoryDao
import com.sofato.krone.data.db.dao.CurrencyDao
import com.sofato.krone.data.db.dao.ExchangeRateDao
import com.sofato.krone.data.db.dao.ExpenseDao
import com.sofato.krone.data.db.dao.IncomeDao
import com.sofato.krone.data.db.dao.MlModelMetadataDao
import com.sofato.krone.data.db.dao.MonthlySnapshotDao
import com.sofato.krone.data.db.dao.RecurringExpenseDao
import com.sofato.krone.data.db.dao.SavingsBalanceSnapshotDao
import com.sofato.krone.data.db.dao.SavingsBucketDao
import com.sofato.krone.data.db.dao.SavingsContributionDao
import com.sofato.krone.data.db.entity.BudgetAllocationEntity
import com.sofato.krone.data.db.entity.CategoryEntity
import com.sofato.krone.data.db.entity.CurrencyEntity
import com.sofato.krone.data.db.entity.ExchangeRateEntity
import com.sofato.krone.data.db.entity.ExpenseEntity
import com.sofato.krone.data.db.entity.IncomeEntity
import com.sofato.krone.data.db.entity.MlModelMetadataEntity
import com.sofato.krone.data.db.entity.MonthlySnapshotEntity
import com.sofato.krone.data.db.entity.RecurringExpenseEntity
import com.sofato.krone.data.db.entity.SavingsBalanceSnapshotEntity
import com.sofato.krone.data.db.entity.SavingsBucketEntity
import com.sofato.krone.data.db.entity.SavingsContributionEntity

@Database(
    entities = [
        CurrencyEntity::class,
        ExchangeRateEntity::class,
        IncomeEntity::class,
        CategoryEntity::class,
        BudgetAllocationEntity::class,
        ExpenseEntity::class,
        RecurringExpenseEntity::class,
        SavingsBucketEntity::class,
        SavingsBalanceSnapshotEntity::class,
        SavingsContributionEntity::class,
        MonthlySnapshotEntity::class,
        MlModelMetadataEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class KroneDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun incomeDao(): IncomeDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun savingsBucketDao(): SavingsBucketDao
    abstract fun savingsBalanceSnapshotDao(): SavingsBalanceSnapshotDao
    abstract fun savingsContributionDao(): SavingsContributionDao
    abstract fun monthlySnapshotDao(): MonthlySnapshotDao
    abstract fun budgetAllocationDao(): BudgetAllocationDao
    abstract fun mlModelMetadataDao(): MlModelMetadataDao
}
