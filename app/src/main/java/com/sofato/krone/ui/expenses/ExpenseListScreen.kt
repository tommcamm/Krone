package com.sofato.krone.ui.expenses

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.R
import com.sofato.krone.ui.components.SwipeToDismissExpenseItem
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onExpenseClick: (Long) -> Unit,
    onAddExpense: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel(),
) {
    val groupedExpenses by viewModel.groupedExpenses.collectAsState()
    val homeCurrency by viewModel.homeCurrency.collectAsState()
    val lastDeleted by viewModel.lastDeletedExpense.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val deletedMessage = stringResource(R.string.expense_deleted)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(lastDeleted) {
        lastDeleted?.let {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long,
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
                title = { Text(stringResource(R.string.recent_expenses)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                    }
                },
                actions = {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            groupedExpenses.forEach { (date, expenses) ->
                item(key = "header_$date") {
                    Text(
                        text = date.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm),
                    )
                }
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
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}
