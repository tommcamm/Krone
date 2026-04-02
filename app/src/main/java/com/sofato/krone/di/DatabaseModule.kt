package com.sofato.krone.di

import android.content.Context
import androidx.room.Room
import com.sofato.krone.data.db.KroneDatabase
import com.sofato.krone.data.db.KroneDatabaseCallback
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
import com.sofato.krone.data.db.migration.Migrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KroneDatabase {
        return Room.databaseBuilder(context, KroneDatabase::class.java, "krone.db")
            .addCallback(KroneDatabaseCallback())
            .addMigrations(*Migrations.ALL_MIGRATIONS)
            .build()
    }

    @Provides fun provideCurrencyDao(db: KroneDatabase): CurrencyDao = db.currencyDao()
    @Provides fun provideCategoryDao(db: KroneDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideExpenseDao(db: KroneDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideExchangeRateDao(db: KroneDatabase): ExchangeRateDao = db.exchangeRateDao()
    @Provides fun provideIncomeDao(db: KroneDatabase): IncomeDao = db.incomeDao()
    @Provides fun provideRecurringExpenseDao(db: KroneDatabase): RecurringExpenseDao = db.recurringExpenseDao()
    @Provides fun provideSavingsBucketDao(db: KroneDatabase): SavingsBucketDao = db.savingsBucketDao()
    @Provides fun provideSavingsBalanceSnapshotDao(db: KroneDatabase): SavingsBalanceSnapshotDao = db.savingsBalanceSnapshotDao()
    @Provides fun provideSavingsContributionDao(db: KroneDatabase): SavingsContributionDao = db.savingsContributionDao()
    @Provides fun provideMonthlySnapshotDao(db: KroneDatabase): MonthlySnapshotDao = db.monthlySnapshotDao()
    @Provides fun provideBudgetAllocationDao(db: KroneDatabase): BudgetAllocationDao = db.budgetAllocationDao()
    @Provides fun provideMlModelMetadataDao(db: KroneDatabase): MlModelMetadataDao = db.mlModelMetadataDao()
}
