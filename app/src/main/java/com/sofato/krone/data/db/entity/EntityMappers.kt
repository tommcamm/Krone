package com.sofato.krone.data.db.entity

import com.sofato.krone.domain.model.BudgetAllocation
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Income
import com.sofato.krone.domain.model.MonthlySnapshot
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.domain.model.SavingsBucket
import com.sofato.krone.domain.model.SavingsContribution
import kotlinx.datetime.Clock

// Currency
fun CurrencyEntity.toDomain(): Currency = Currency(
    code = code, name = name, symbol = symbol, decimalPlaces = decimalPlaces,
    symbolPosition = symbolPosition, isEnabled = isEnabled, sortOrder = sortOrder,
)
fun Currency.toEntity(): CurrencyEntity = CurrencyEntity(
    code = code, name = name, symbol = symbol, decimalPlaces = decimalPlaces,
    symbolPosition = symbolPosition, isEnabled = isEnabled, sortOrder = sortOrder,
)

// Category
fun CategoryEntity.toDomain(): Category = Category(
    id = id, name = name, iconName = iconName, colorHex = colorHex,
    isCustom = isCustom, sortOrder = sortOrder, isArchived = isArchived,
)
fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id, name = name, iconName = iconName, colorHex = colorHex,
    isCustom = isCustom, sortOrder = sortOrder, isArchived = isArchived,
)

// Income
fun IncomeEntity.toDomain(): Income = Income(
    id = id, amountMinor = amountMinor, currencyCode = currencyCode,
    label = label, isRecurring = isRecurring, recurrenceRule = recurrenceRule,
    startDate = startDate, createdAt = createdAt,
)
fun Income.toEntity(): IncomeEntity = IncomeEntity(
    id = id, amountMinor = amountMinor, currencyCode = currencyCode,
    label = label, isRecurring = isRecurring, recurrenceRule = recurrenceRule,
    startDate = startDate, createdAt = if (id == 0L) Clock.System.now() else createdAt,
)

// RecurringExpense
fun RecurringExpenseEntity.toDomain(): RecurringExpense = RecurringExpense(
    id = id, amountMinor = amountMinor, currencyCode = currencyCode,
    categoryId = categoryId, label = label, recurrenceRule = RecurrenceRule.normalize(recurrenceRule),
    nextDate = nextDate, isActive = isActive, createdAt = createdAt,
)
fun RecurringExpense.toEntity(): RecurringExpenseEntity = RecurringExpenseEntity(
    id = id, amountMinor = amountMinor, currencyCode = currencyCode,
    categoryId = categoryId, label = label, recurrenceRule = RecurrenceRule.normalize(recurrenceRule),
    nextDate = nextDate, isActive = isActive,
    createdAt = if (id == 0L) Clock.System.now() else createdAt,
)

// SavingsBucket
fun SavingsBucketEntity.toDomain(): SavingsBucket = SavingsBucket(
    id = id, label = label, type = type, currencyCode = currencyCode,
    monthlyContributionMinor = monthlyContributionMinor,
    targetAmountMinor = targetAmountMinor, deadline = deadline,
    currentBalanceMinor = currentBalanceMinor, isActive = isActive, sortOrder = sortOrder,
)
fun SavingsBucket.toEntity(): SavingsBucketEntity = SavingsBucketEntity(
    id = id, label = label, type = type, currencyCode = currencyCode,
    monthlyContributionMinor = monthlyContributionMinor,
    targetAmountMinor = targetAmountMinor, deadline = deadline,
    currentBalanceMinor = currentBalanceMinor, balanceUpdatedAt = null,
    isActive = isActive, sortOrder = sortOrder, createdAt = Clock.System.now(),
)

// SavingsContribution
fun SavingsContributionEntity.toDomain(): SavingsContribution = SavingsContribution(
    id = id, bucketId = bucketId, amountMinor = amountMinor,
    date = date, isAutoPosted = isAutoPosted, isSkipped = isSkipped,
)
fun SavingsContribution.toEntity(): SavingsContributionEntity = SavingsContributionEntity(
    id = id, bucketId = bucketId, amountMinor = amountMinor,
    date = date, isAutoPosted = isAutoPosted, isSkipped = isSkipped,
)

// BudgetAllocation
fun BudgetAllocationEntity.toDomain(): BudgetAllocation = BudgetAllocation(
    id = id, categoryId = categoryId, month = month,
    allocatedAmountMinor = allocatedAmountMinor, currencyCode = currencyCode,
)
fun BudgetAllocation.toEntity(): BudgetAllocationEntity = BudgetAllocationEntity(
    id = id, categoryId = categoryId, month = month,
    allocatedAmountMinor = allocatedAmountMinor, currencyCode = currencyCode,
)

// MonthlySnapshot
fun MonthlySnapshotEntity.toDomain(): MonthlySnapshot = MonthlySnapshot(
    id = id, month = month, totalIncomeMinor = totalIncomeMinor,
    totalFixedMinor = totalFixedMinor, totalVariableMinor = totalVariableMinor,
    totalSavingsMinor = totalSavingsMinor, homeCurrencyCode = homeCurrencyCode,
)
fun MonthlySnapshot.toEntity(): MonthlySnapshotEntity = MonthlySnapshotEntity(
    id = id, month = month, totalIncomeMinor = totalIncomeMinor,
    totalFixedMinor = totalFixedMinor, totalVariableMinor = totalVariableMinor,
    totalSavingsMinor = totalSavingsMinor, homeCurrencyCode = homeCurrencyCode,
)
