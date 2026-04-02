package com.sofato.krone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sofato.krone.ui.budget.BudgetScreen
import com.sofato.krone.ui.dashboard.DashboardScreen
import com.sofato.krone.ui.expenses.AddExpenseScreen
import com.sofato.krone.ui.expenses.CategoryManagementScreen
import com.sofato.krone.ui.expenses.EditExpenseScreen
import com.sofato.krone.ui.expenses.ExpenseListScreen
import com.sofato.krone.ui.insights.InsightsPlaceholderScreen
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
) {
    NavHost(
        navController = navController,
        startDestination = KroneDestination.Dashboard,
        modifier = modifier,
    ) {
        composable<KroneDestination.Dashboard> {
            DashboardScreen(
                onAddExpense = { categoryId ->
                    navController.navigate(KroneDestination.AddExpense(categoryId = categoryId ?: -1L))
                },
                onExpenseClick = { id -> navController.navigate(KroneDestination.EditExpense(id)) },
                onViewAllExpenses = { navController.navigate(KroneDestination.ExpenseList) },
                onManageCommitments = { navController.navigate(KroneDestination.RecurringExpenseList) },
            )
        }
        composable<KroneDestination.Budget> {
            BudgetScreen(
                onManageCommitments = { navController.navigate(KroneDestination.RecurringExpenseList) },
            )
        }
        composable<KroneDestination.Savings> {
            SavingsScreen(
                onBucketClick = { id -> navController.navigate(KroneDestination.SavingsBucketDetail(id)) },
            )
        }
        composable<KroneDestination.Insights> {
            InsightsPlaceholderScreen()
        }
        composable<KroneDestination.AddExpense> {
            AddExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
                onManageCategories = { navController.navigate(KroneDestination.CategoryManagement) },
            )
        }
        composable<KroneDestination.EditExpense> {
            EditExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<KroneDestination.ExpenseList> {
            ExpenseListScreen(
                onExpenseClick = { id -> navController.navigate(KroneDestination.EditExpense(id)) },
                onAddExpense = { navController.navigate(KroneDestination.AddExpense()) },
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
    }
}
