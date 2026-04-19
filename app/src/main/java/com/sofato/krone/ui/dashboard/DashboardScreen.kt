package com.sofato.krone.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sofato.krone.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.ui.components.SwipeToDismissExpenseItem
import com.sofato.krone.ui.dashboard.components.ArcSegment
import com.sofato.krone.ui.dashboard.components.BudgetArcChart
import com.sofato.krone.ui.dashboard.components.StatsRow
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import java.time.Month as JavaMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import androidx.core.graphics.toColorInt

@Composable
fun DashboardScreen(
    onAddExpense: (categoryId: Long?) -> Unit,
    onViewAllExpenses: () -> Unit,
    onEditExpense: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val homeCurrency by viewModel.homeCurrency.collectAsState()
    val dailyBudget by viewModel.dailyBudget.collectAsState()
    val totalSpentToday by viewModel.totalSpentToday.collectAsState()
    val rollingAvg by viewModel.rollingDailyAverage.collectAsState()
    val budgetOverview by viewModel.budgetOverview.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val lastDeleted by viewModel.lastDeletedExpense.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    val currency = homeCurrency ?: return
    val budget = dailyBudget ?: return

    val totalSpent = budget.spentSoFarMinor + totalSpentToday
    val monthNumber = budgetOverview?.period?.startDate?.month?.number
        ?: LocalDate.today().month.number
    val monthName = JavaMonth.of(monthNumber)
        .getDisplayName(JavaTextStyle.FULL_STANDALONE, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }

    // Build arc segments from budget overview
    val overview = budgetOverview
    val segments = buildArcSegments(overview)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.SpacingMd),
        ) {
            // Header
            Spacer(Modifier.height(Dimens.SpacingSm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "$monthName ▾",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
            }

            // Arc chart
            BudgetArcChart(
                totalBudget = budget.discretionaryMinor,
                totalSpent = totalSpent,
                remainingDays = budget.remainingDays,
                currency = currency,
                segments = segments,
            )

            Spacer(Modifier.height(Dimens.SpacingSm))

            // Add expense button
            OutlinedButton(
                onClick = { onAddExpense(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Add expense",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = Dimens.SpacingSm),
                )
            }

            Spacer(Modifier.height(Dimens.SpacingSm))

            // Stats row
            val availableToday = budget.dailyAmountMinor - totalSpentToday
            StatsRow(
                availableToday = availableToday,
                dailyAverage = rollingAvg,
                onTrack = availableToday >= 0,
                currency = currency,
            )

            Spacer(Modifier.height(Dimens.SpacingMd))

            // Recent expenses section — header + preview of the 5 latest
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.recent_expenses),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onViewAllExpenses) {
                    Text(stringResource(R.string.view_all))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                    )
                }
            }

            if (recentExpenses.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_expenses_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Dimens.SpacingMd),
                )
            } else {
                Column {
                    recentExpenses.forEachIndexed { index, expense ->
                        SwipeToDismissExpenseItem(
                            expense = expense,
                            onDismiss = { viewModel.deleteExpense(expense) },
                            onClick = { onEditExpense(expense.id) },
                            homeCurrency = currency,
                        )
                        if (index < recentExpenses.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimens.FabSpacerHeight))
        }
    }
}

internal fun buildArcSegments(
    overview: com.sofato.krone.domain.model.BudgetOverview?,
): List<ArcSegment> {
    if (overview == null) return emptyList()

    return overview.categoryBreakdown
        .filter { it.spentMinor > 0 }
        .sortedByDescending { it.spentMinor }
        .map { cs ->
            val categoryColor = try {
                Color(cs.category.colorHex.toColorInt())
            } catch (_: Exception) {
                Color(0xFF94A3B8) // fallback slate
            }
            ArcSegment(
                label = cs.category.name,
                value = cs.spentMinor,
                color = categoryColor,
            )
        }
}
