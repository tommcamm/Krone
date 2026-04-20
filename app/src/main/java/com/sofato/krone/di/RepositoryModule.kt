package com.sofato.krone.di

import com.sofato.krone.data.repository.ExchangeRateRepositoryImpl
import com.sofato.krone.data.repository.BudgetAllocationRepositoryImpl
import com.sofato.krone.data.repository.CategoryRepositoryImpl
import com.sofato.krone.data.repository.CurrencyRepositoryImpl
import com.sofato.krone.data.repository.ExpenseRepositoryImpl
import com.sofato.krone.data.repository.IncomeRepositoryImpl
import com.sofato.krone.data.repository.MonthlySnapshotRepositoryImpl
import com.sofato.krone.data.repository.RecurringExpenseRepositoryImpl
import com.sofato.krone.data.repository.SavingsBucketRepositoryImpl
import com.sofato.krone.data.repository.SavingsContributionRepositoryImpl
import com.sofato.krone.data.repository.UserPreferencesRepositoryImpl
import com.sofato.krone.domain.repository.BudgetAllocationRepository
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExchangeRateRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.repository.IncomeRepository
import com.sofato.krone.domain.repository.MonthlySnapshotRepository
import com.sofato.krone.domain.repository.RecurringExpenseRepository
import com.sofato.krone.domain.repository.SavingsBucketRepository
import com.sofato.krone.domain.repository.SavingsContributionRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.groups.data.repository.DeviceIdentityRepositoryImpl
import com.sofato.krone.groups.data.repository.ServerEnrollmentRepositoryImpl
import com.sofato.krone.groups.domain.repository.DeviceIdentityRepository
import com.sofato.krone.groups.domain.repository.ServerEnrollmentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindCurrencyRepository(impl: CurrencyRepositoryImpl): CurrencyRepository

    @Binds @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds @Singleton
    abstract fun bindIncomeRepository(impl: IncomeRepositoryImpl): IncomeRepository

    @Binds @Singleton
    abstract fun bindRecurringExpenseRepository(impl: RecurringExpenseRepositoryImpl): RecurringExpenseRepository

    @Binds @Singleton
    abstract fun bindSavingsBucketRepository(impl: SavingsBucketRepositoryImpl): SavingsBucketRepository

    @Binds @Singleton
    abstract fun bindSavingsContributionRepository(impl: SavingsContributionRepositoryImpl): SavingsContributionRepository

    @Binds @Singleton
    abstract fun bindBudgetAllocationRepository(impl: BudgetAllocationRepositoryImpl): BudgetAllocationRepository

    @Binds @Singleton
    abstract fun bindMonthlySnapshotRepository(impl: MonthlySnapshotRepositoryImpl): MonthlySnapshotRepository

    @Binds @Singleton
    abstract fun bindExchangeRateRepository(impl: ExchangeRateRepositoryImpl): ExchangeRateRepository

    @Binds @Singleton
    abstract fun bindDeviceIdentityRepository(impl: DeviceIdentityRepositoryImpl): DeviceIdentityRepository

    @Binds @Singleton
    abstract fun bindServerEnrollmentRepository(impl: ServerEnrollmentRepositoryImpl): ServerEnrollmentRepository
}
