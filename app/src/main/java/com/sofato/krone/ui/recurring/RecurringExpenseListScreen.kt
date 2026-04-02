package com.sofato.krone.ui.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.RecurringExpense
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpenseListScreen(
    onAddRecurring: () -> Unit,
    onExpenseClick: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RecurringExpenseListViewModel = hiltViewModel(),
) {
    val expenses by viewModel.recurringExpenses.collectAsState()
    val currency by viewModel.homeCurrency.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Expenses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecurring) {
                Icon(Icons.Default.Add, contentDescription = "Add recurring expense")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(items = expenses, key = { it.id }) { expense ->
                currency?.let { curr ->
                    SwipeToDismissRecurringItem(
                        expense = expense,
                        currency = curr,
                        onDismiss = { viewModel.deactivate(expense.id) },
                        onClick = { onExpenseClick(expense.id) },
                        modifier = Modifier.animateItem(),
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = Dimens.SpacingMd))
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissRecurringItem(
    expense: RecurringExpense,
    currency: Currency,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm + Dimens.SpacingXs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = expense.label,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "${recurrenceLabel(expense.recurrenceRule)} · Next: ${expense.nextDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = CurrencyFormatter.formatDisplay(expense.amountMinor, currency),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

private fun recurrenceLabel(rule: String): String {
    return when (RecurrenceRule.normalize(rule)) {
        RecurrenceRule.YEARLY -> "Yearly"
        else -> "Monthly"
    }
}
