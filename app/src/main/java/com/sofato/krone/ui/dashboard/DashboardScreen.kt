package com.sofato.krone.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.ui.dashboard.components.ArcSegment
import com.sofato.krone.ui.dashboard.components.BudgetArcChart
import com.sofato.krone.ui.dashboard.components.ProjectionCard
import com.sofato.krone.ui.dashboard.components.StatsRow
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.today
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import java.time.Month as JavaMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    onAddExpense: (categoryId: Long?) -> Unit,
    onViewAllExpenses: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val homeCurrency by viewModel.homeCurrency.collectAsState()
    val dailyBudget by viewModel.dailyBudget.collectAsState()
    val totalSpentToday by viewModel.totalSpentToday.collectAsState()
    val rollingAvg by viewModel.rollingDailyAverage.collectAsState()
    val budgetOverview by viewModel.budgetOverview.collectAsState()
    val projectedEndOfMonth by viewModel.projectedEndOfMonth.collectAsState()

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
    val segments = buildArcSegments(overview, budget.totalFixedMinor)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpacingMd),
    ) {
        // Header
        Spacer(Modifier.height(Dimens.SpacingSm))
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

        // Arc chart
        BudgetArcChart(
            totalBudget = budget.discretionaryMinor,
            totalSpent = totalSpent,
            remainingDays = budget.remainingDays,
            currency = currency,
            segments = segments,
        )

        // Arc chart legend
        if (segments.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.SpacingSm),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
            ) {
                segments.forEach { segment ->
                    val pct = if (budget.discretionaryMinor > 0) {
                        (segment.value * 100 / budget.discretionaryMinor).toInt()
                    } else 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawCircle(color = segment.color)
                        }
                        Spacer(Modifier.width(Dimens.SpacingXs))
                        Text(
                            text = "${segment.label} $pct%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

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
        val onTrack = projectedEndOfMonth <= 0
        StatsRow(
            availableToday = availableToday,
            dailyAverage = rollingAvg,
            onTrack = onTrack,
            currency = currency,
        )

        Spacer(Modifier.height(Dimens.SpacingSm))

        // Projection card
        ProjectionCard(
            projectedDifference = projectedEndOfMonth,
            currency = currency,
        )

        Spacer(Modifier.height(Dimens.SpacingMd))

        // Recent expenses link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onViewAllExpenses)
                .padding(vertical = Dimens.SpacingMd),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recent expenses",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View all expenses",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(Dimens.FabSpacerHeight))
    }
}

internal fun buildArcSegments(
    overview: com.sofato.krone.domain.model.BudgetOverview?,
    fixedMinor: Long,
): List<ArcSegment> {
    if (overview == null) return emptyList()

    val segments = mutableListOf<ArcSegment>()

    // Fixed expenses as one segment
    if (fixedMinor > 0) {
        segments.add(
            ArcSegment(
                label = "Fixed",
                value = fixedMinor,
                color = Color(0xFF6366F1), // Indigo
            ),
        )
    }

    // Category spending segments
    val trackedCategories = overview.categoryBreakdown.filter { it.spentMinor > 0 }
    for (cs in trackedCategories) {
        val categoryColor = try {
            Color(cs.category.colorHex.toColorInt())
        } catch (_: Exception) {
            Color(0xFF94A3B8) // fallback slate
        }
        segments.add(
            ArcSegment(
                label = cs.category.name,
                value = cs.spentMinor,
                color = categoryColor,
            ),
        )
    }

    return segments
}
