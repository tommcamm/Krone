package com.sofato.krone.ui.expenses

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.R
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.ui.components.SwipeToDismissExpenseItem
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter
import kotlinx.datetime.number

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onExpenseClick: (Long) -> Unit,
    onAddExpense: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel(),
) {
    val expenses by viewModel.expenses.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val sort by viewModel.sort.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val homeCurrency by viewModel.homeCurrency.collectAsState()
    val lastDeleted by viewModel.lastDeletedExpense.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showFilterSheet by remember { mutableStateOf(false) }

    val deletedMessage = stringResource(R.string.expense_deleted)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(lastDeleted) {
        lastDeleted?.let {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearDeletedExpense()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.all_expenses)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (filter.isActive) {
                                    Badge()
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = stringResource(R.string.filter_and_sort),
                            )
                        }
                    }
                    IconButton(onClick = onAddExpense) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_expense))
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (filter.isActive) {
                ActiveFilterChipsRow(
                    filter = filter,
                    categories = categories,
                    homeCurrency = homeCurrency,
                    onDismissDate = viewModel::dismissDateRange,
                    onDismissCategory = viewModel::dismissCategory,
                    onDismissName = viewModel::dismissNameQuery,
                    onDismissAmount = viewModel::dismissAmountRange,
                    onClearAll = viewModel::clearFilters,
                )
                HorizontalDivider()
            }

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.SpacingLg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.expense_list_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                ExpenseList(
                    expenses = expenses,
                    groupByDate = sort.groupsByDate,
                    homeCurrency = homeCurrency,
                    onClick = onExpenseClick,
                    onDelete = viewModel::deleteExpense,
                )
            }
        }
    }

    if (showFilterSheet) {
        ExpenseFilterSortBottomSheet(
            currentFilter = filter,
            currentSort = sort,
            categories = categories,
            homeCurrency = homeCurrency,
            onApply = { newFilter, newSort ->
                viewModel.updateFilter(newFilter)
                viewModel.updateSort(newSort)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false },
        )
    }
}

@Composable
private fun ExpenseList(
    expenses: List<com.sofato.krone.domain.model.Expense>,
    groupByDate: Boolean,
    homeCurrency: Currency?,
    onClick: (Long) -> Unit,
    onDelete: (com.sofato.krone.domain.model.Expense) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (groupByDate) {
            val grouped = expenses.groupBy { it.date }
            grouped.forEach { (date, items) ->
                item(key = "header_$date") {
                    val formattedDate = remember(date) {
                        java.time.LocalDate.of(date.year, date.month.number, date.day)
                            .format(java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM))
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm),
                    )
                }
                items(items = items, key = { it.id }) { expense ->
                    SwipeToDismissExpenseItem(
                        expense = expense,
                        onDismiss = { onDelete(expense) },
                        onClick = { onClick(expense.id) },
                        modifier = Modifier.animateItem(),
                        homeCurrency = homeCurrency,
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 68.dp))
                }
            }
        } else {
            items(items = expenses, key = { it.id }) { expense ->
                SwipeToDismissExpenseItem(
                    expense = expense,
                    onDismiss = { onDelete(expense) },
                    onClick = { onClick(expense.id) },
                    modifier = Modifier.animateItem(),
                    homeCurrency = homeCurrency,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 68.dp))
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveFilterChipsRow(
    filter: ExpenseFilter,
    categories: List<Category>,
    homeCurrency: Currency?,
    onDismissDate: () -> Unit,
    onDismissCategory: (Long) -> Unit,
    onDismissName: () -> Unit,
    onDismissAmount: () -> Unit,
    onClearAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingSm, vertical = Dimens.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (filter.dateRange !is DateRange.AllTime) {
                ActiveChip(label = dateRangeLabel(filter.dateRange), onDismiss = onDismissDate)
            }
            filter.categoryIds.forEach { id ->
                val name = categories.firstOrNull { it.id == id }?.name ?: return@forEach
                ActiveChip(label = name, onDismiss = { onDismissCategory(id) })
            }
            if (filter.nameQuery.isNotBlank()) {
                ActiveChip(label = "\"${filter.nameQuery}\"", onDismiss = onDismissName)
            }
            if (filter.minAmountMinor != null || filter.maxAmountMinor != null) {
                ActiveChip(
                    label = amountRangeLabel(filter.minAmountMinor, filter.maxAmountMinor, homeCurrency),
                    onDismiss = onDismissAmount,
                )
            }
        }
        TextButton(onClick = onClearAll) {
            Text(stringResource(R.string.filter_clear_all))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveChip(label: String, onDismiss: () -> Unit) {
    InputChip(
        selected = true,
        onClick = onDismiss,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(InputChipDefaults.IconSize),
            )
        },
    )
}

@Composable
private fun dateRangeLabel(range: DateRange): String = when (range) {
    DateRange.AllTime -> stringResource(R.string.filter_date_all_time)
    DateRange.ThisMonth -> stringResource(R.string.filter_date_this_month)
    DateRange.LastMonth -> stringResource(R.string.filter_date_last_month)
    DateRange.Last3Months -> stringResource(R.string.filter_date_last_3_months)
    DateRange.ThisYear -> stringResource(R.string.filter_date_this_year)
    is DateRange.Custom -> stringResource(
        R.string.filter_date_custom_range,
        range.start.toString(),
        range.end.toString(),
    )
}

@Composable
private fun amountRangeLabel(min: Long?, max: Long?, homeCurrency: Currency?): String {
    val decimals = homeCurrency?.decimalPlaces ?: 2
    val symbol = homeCurrency?.code ?: ""
    val minStr = min?.let { CurrencyFormatter.formatPlain(it, decimals) } ?: "—"
    val maxStr = max?.let { CurrencyFormatter.formatPlain(it, decimals) } ?: "—"
    return "$symbol $minStr – $maxStr".trim()
}
