package com.sofato.krone.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    onManageRecurring: () -> Unit,
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
                )
            }
        }

        // Income section
        if (incomeList.isNotEmpty()) {
            item(key = "income_header") {
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Dimens.SpacingSm),
                )
            }
            items(items = incomeList, key = { it.id }) { income ->
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
                TextButton(onClick = onManageRecurring) {
                    Text("Manage")
                }
            }
        }

        items(items = recurringExpenses, key = { it.id }) { expense ->
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

        // Category breakdown
        overview?.let { ov ->
            if (ov.categoryBreakdown.isNotEmpty()) {
                item(key = "category_header") {
                    Text(
                        text = "Category breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = Dimens.SpacingSm),
                    )
                }
                items(
                    items = ov.categoryBreakdown,
                    key = { it.category.id },
                ) { categorySpend ->
                    CategoryProgressBar(
                        category = categorySpend.category,
                        spentMinor = categorySpend.spentMinor,
                        allocatedMinor = categorySpend.allocatedMinor,
                        currency = curr,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(Dimens.SpacingXxl)) }
    }
}
