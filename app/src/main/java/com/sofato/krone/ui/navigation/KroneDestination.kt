package com.sofato.krone.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface KroneDestination {
    @Serializable data object Dashboard : KroneDestination
    @Serializable data object Budget : KroneDestination
    @Serializable data object Savings : KroneDestination
    @Serializable data object Insights : KroneDestination
    @Serializable data class AddExpense(val categoryId: Long = -1L) : KroneDestination
    @Serializable data class EditExpense(val expenseId: Long) : KroneDestination
    @Serializable data object ExpenseList : KroneDestination
    @Serializable data object CategoryManagement : KroneDestination
    @Serializable data object Onboarding : KroneDestination
    @Serializable data object ManageCommitments : KroneDestination
    @Serializable data object RecurringExpenseList : KroneDestination
    @Serializable data object AddRecurringExpense : KroneDestination
    @Serializable data class EditRecurringExpense(val expenseId: Long) : KroneDestination
    @Serializable data object AddSavingsBucket : KroneDestination
    @Serializable data class EditSavingsBucket(val bucketId: Long) : KroneDestination
    @Serializable data class SavingsBucketDetail(val bucketId: Long) : KroneDestination
    @Serializable data object ManageSalary : KroneDestination
}
