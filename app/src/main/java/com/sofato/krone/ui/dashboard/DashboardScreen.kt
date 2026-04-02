package com.sofato.krone.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.ui.components.SwipeToDismissExpenseItem
import com.sofato.krone.ui.dashboard.components.BudgetBreakdownCard
import com.sofato.krone.ui.dashboard.components.DailyBudgetHeroCard
import com.sofato.krone.ui.dashboard.components.QuickAddRow
import com.sofato.krone.ui.dashboard.components.StatsRow
import com.sofato.krone.ui.theme.Dimens

@Composable
fun DashboardScreen(
    onAddExpense: (categoryId: Long?) -> Unit,
    onExpenseClick: (Long) -> Unit,
    onViewAllExpenses: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val expenses by viewModel.todaysExpenses.collectAsState()
    val totalSpent by viewModel.totalSpentToday.collectAsState()
    val homeCurrency by viewModel.homeCurrency.collectAsState()
    val dailyBudget by viewModel.dailyBudget.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val rollingAvg by viewModel.rollingDailyAverage.collectAsState()
    val lastDeleted by viewModel.lastDeletedExpense.collectAsState()
    val budgetOverview by viewModel.budgetOverview.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lastDeleted) {
        lastDeleted?.let {
            val result = snackbarHostState.showSnackbar(
                message = "Expense deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearDeletedExpense()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Monthly budget breakdown
            item {
                val currency = homeCurrency
                val budget = dailyBudget
                if (currency != null && budget != null) {
                    val ov = budgetOverview
                    val trackedCategories = ov
                        ?.categoryBreakdown
                        ?.filter { it.allocatedMinor > 0 }
                        ?: emptyList()
                    BudgetBreakdownCard(
                        dailyBudget = budget,
                        spentToday = totalSpent,
                        currency = currency,
                        modifier = Modifier.padding(Dimens.SpacingMd),
                        trackedCategories = trackedCategories,
                        totalAllocatedMinor = ov?.totalAllocatedMinor ?: 0L,
                        unallocatedDiscretionaryMinor = ov?.unallocatedDiscretionaryMinor ?: budget.discretionaryMinor,
                    )
                }
            }

            // Daily Budget Hero Card
            item {
                val currency = homeCurrency
                val budget = dailyBudget
                if (currency != null && budget != null) {
                    val heroTracked = budgetOverview
                        ?.categoryBreakdown
                        ?.filter { it.allocatedMinor > 0 }
                        ?: emptyList()
                    DailyBudgetHeroCard(
                        dailyBudget = budget,
                        spentToday = totalSpent,
                        currency = currency,
                        modifier = Modifier
                            .padding(horizontal = Dimens.SpacingMd)
                            .animateContentSize(spring(stiffness = Spring.StiffnessLow)),
                        trackedCategories = heroTracked,
                    )
                }
            }

            // Quick add row
            if (categories.isNotEmpty()) {
                item {
                    Text(
                        text = "Quick add",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = Dimens.SpacingMd, top = Dimens.SpacingSm),
                    )
                    QuickAddRow(
                        categories = categories,
                        onCategoryClick = { category -> onAddExpense(category.id) },
                        modifier = Modifier.padding(vertical = Dimens.SpacingXs),
                    )
                }
            }

            // Stats row
            item {
                val currency = homeCurrency
                val budget = dailyBudget
                if (currency != null && budget != null) {
                    val monthlyRemaining = budget.discretionaryMinor - budget.spentSoFarMinor - totalSpent
                    StatsRow(
                        dailyAverage = rollingAvg,
                        monthlyRemaining = monthlyRemaining,
                        remainingDays = budget.remainingDays,
                        currency = currency,
                        modifier = Modifier.padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm),
                    )
                }
            }

            // Today's expenses header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Today's expenses",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onViewAllExpenses) {
                        Text("View all")
                    }
                }
            }

            if (expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.SpacingXxl),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No expenses today",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(items = expenses, key = { it.id }) { expense ->
                    SwipeToDismissExpenseItem(
                        expense = expense,
                        onDismiss = { viewModel.deleteExpense(expense) },
                        onClick = { onExpenseClick(expense.id) },
                        modifier = Modifier.animateItem(),
                        homeCurrency = homeCurrency,
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 68.dp))
                }
            }

            item { Spacer(Modifier.height(Dimens.FabSpacerHeight)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
