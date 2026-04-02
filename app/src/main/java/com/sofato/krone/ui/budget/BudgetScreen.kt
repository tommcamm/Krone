package com.sofato.krone.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.ui.budget.components.BudgetOverviewCard
import com.sofato.krone.ui.budget.components.CategoryProgressBar
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun BudgetScreen(
    onManageCommitments: () -> Unit,
    onManageSalary: () -> Unit,
    onManageBudgets: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val overview by viewModel.budgetOverview.collectAsState()
    val recurringExpenses by viewModel.recurringExpenses.collectAsState()
    val incomeList by viewModel.income.collectAsState()
    val currency by viewModel.homeCurrency.collectAsState()

    val curr = currency ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
    ) {
        item { Spacer(Modifier.height(Dimens.SpacingMd)) }

        // Budget overview card
        overview?.let { ov ->
            item(key = "overview") {
                BudgetOverviewCard(
                    totalIncome = ov.totalIncomeMinor,
                    totalFixed = ov.totalFixedMinor,
                    totalSavings = ov.totalSavingsMinor,
                    discretionary = ov.discretionaryMinor,
                    currency = curr,
                    categoryBreakdown = ov.categoryBreakdown,
                    unallocatedDiscretionaryMinor = ov.unallocatedDiscretionaryMinor,
                )
            }
        }

        // Income section
        if (incomeList.isNotEmpty()) {
            item(key = "income_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    TextButton(onClick = onManageSalary) {
                        Text("Manage salary")
                    }
                }
            }
            items(items = incomeList, key = { "income_${it.id}" }) { income ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = income.label,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = CurrencyFormatter.formatDisplay(income.amountMinor, curr),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // Fixed expenses section
        item(key = "fixed_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Fixed expenses",
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onManageCommitments) {
                    Text("Manage commitments")
                }
            }
        }

        items(items = recurringExpenses, key = { "recurring_${it.id}" }) { expense ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = expense.label,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = CurrencyFormatter.formatDisplay(expense.amountMinor, curr),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        // Category budget breakdown
        overview?.let { ov ->
            item(key = "category_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Category budgets",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    TextButton(onClick = onManageBudgets) {
                        Text("Manage budgets")
                    }
                }
            }

            if (ov.categoryBreakdown.isNotEmpty()) {
                items(
                    items = ov.categoryBreakdown,
                    key = { "cat_${it.category.id}" },
                ) { categorySpend ->
                    CategoryProgressBar(
                        category = categorySpend.category,
                        spentMinor = categorySpend.spentMinor,
                        allocatedMinor = categorySpend.allocatedMinor,
                        currency = curr,
                    )
                }
            }

            // Unallocated discretionary
            if (ov.totalAllocatedMinor > 0) {
                item(key = "unallocated") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.SpacingXs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Unallocated",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = CurrencyFormatter.formatDisplay(ov.unallocatedDiscretionaryMinor, curr),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (ov.unallocatedDiscretionaryMinor < 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(Dimens.FabSpacerHeight)) }
    }
}
