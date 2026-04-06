package com.sofato.krone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sofato.krone.ui.budget.BudgetScreen
import com.sofato.krone.ui.budget.MonthlyBudgetsScreen
import com.sofato.krone.ui.currency.CurrencySettingsScreen
import com.sofato.krone.ui.income.ManageSalaryScreen
import com.sofato.krone.ui.settings.SettingsScreen
import com.sofato.krone.ui.dashboard.DashboardScreen
import com.sofato.krone.ui.expenses.CategoryManagementScreen
import com.sofato.krone.ui.expenses.ExpenseListScreen
import com.sofato.krone.ui.insights.InsightsScreen
import com.sofato.krone.ui.recurring.AddRecurringExpenseScreen
import com.sofato.krone.ui.recurring.EditRecurringExpenseScreen
import com.sofato.krone.ui.recurring.RecurringExpenseListScreen
import com.sofato.krone.ui.savings.AddSavingsBucketScreen
import com.sofato.krone.ui.savings.EditSavingsBucketScreen
import com.sofato.krone.ui.savings.SavingsBucketDetailScreen
import com.sofato.krone.ui.savings.SavingsScreen

@Composable
fun KroneNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onAddExpense: (categoryId: Long?) -> Unit,
    onEditExpense: (expenseId: Long) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = KroneDestination.Dashboard,
        modifier = modifier,
    ) {
        composable<KroneDestination.Dashboard> {
            DashboardScreen(
                onAddExpense = { categoryId -> onAddExpense(categoryId) },
                onViewAllExpenses = { navController.navigate(KroneDestination.ExpenseList) },
            )
        }
        composable<KroneDestination.Budget> {
            BudgetScreen(
                onManageCommitments = { navController.navigate(KroneDestination.RecurringExpenseList) },
                onManageSalary = { navController.navigate(KroneDestination.ManageSalary) },
                onManageBudgets = { navController.navigate(KroneDestination.MonthlyBudgets) },
            )
        }
        composable<KroneDestination.MonthlyBudgets> {
            MonthlyBudgetsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.Savings> {
            SavingsScreen(
                onBucketClick = { id -> navController.navigate(KroneDestination.SavingsBucketDetail(id)) },
            )
        }
        composable<KroneDestination.Insights> {
            InsightsScreen()
        }
        composable<KroneDestination.ExpenseList> {
            ExpenseListScreen(
                onExpenseClick = { id -> onEditExpense(id) },
                onAddExpense = { onAddExpense(null) },
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.CategoryManagement> {
            CategoryManagementScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.RecurringExpenseList> {
            RecurringExpenseListScreen(
                onAddRecurring = { navController.navigate(KroneDestination.AddRecurringExpense) },
                onExpenseClick = { id -> navController.navigate(KroneDestination.EditRecurringExpense(id)) },
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.AddRecurringExpense> {
            AddRecurringExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.EditRecurringExpense> {
            EditRecurringExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.AddSavingsBucket> {
            AddSavingsBucketScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.EditSavingsBucket> {
            EditSavingsBucketScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.SavingsBucketDetail> {
            SavingsBucketDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(KroneDestination.EditSavingsBucket(id)) },
            )
        }
        composable<KroneDestination.ManageSalary> {
            ManageSalaryScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.CurrencySettings> {
            CurrencySettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.Settings> {
            SettingsScreen(
                onNavigateToCurrency = { navController.navigate(KroneDestination.CurrencySettings) },
                onNavigateToCategories = { navController.navigate(KroneDestination.CategoryManagement) },
            )
        }
    }
}
