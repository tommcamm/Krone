package com.sofato.krone.ui.onboarding.steps

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.SavingsBucketType
import com.sofato.krone.ui.onboarding.OnboardingViewModel
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

private data class SavingsTemplate(
    val label: String,
    val type: SavingsBucketType,
)

private val templates = listOf(
    SavingsTemplate("Emergency fund", SavingsBucketType.EMERGENCY_FUND),
    SavingsTemplate("ASK", SavingsBucketType.ASK),
    SavingsTemplate("Pension", SavingsBucketType.PENSION),
    SavingsTemplate("Feriepenge buffer", SavingsBucketType.FERIEPENGE),
    SavingsTemplate("Custom", SavingsBucketType.GOAL),
)

@Composable
fun SavingsGoalsStep(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val goals by viewModel.savingsGoals.collectAsState()
    val selectedCurrencyCode by viewModel.selectedCurrencyCode.collectAsState()
    val monthlyTotalMinor = goals.sumOf { it.monthlyContributionMinor }

    // Local text state for monthly and target fields
    val monthlyTexts = remember { mutableStateMapOf<Int, String>() }
    val targetTexts = remember { mutableStateMapOf<Int, String>() }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
                Text(
                    text = "Planned savings per month",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${CurrencyFormatter.formatPlain(monthlyTotalMinor, 2)} $selectedCurrencyCode",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                Text(
                    text = "Add at least one bucket to keep savings visible in your budget.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // Template chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        ) {
            templates.forEach { template ->
                AssistChip(
                    onClick = {
                        viewModel.addSavingsGoal(
                            label = template.label,
                            type = template.type,
                            monthlyMinor = 0L,
                            targetMinor = if (template.type == SavingsBucketType.EMERGENCY_FUND) 0L else null,
                        )
                    },
                    label = { Text(template.label) },
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // Added goals
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        ) {
            itemsIndexed(goals, key = { index, _ -> index }) { index, goal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.SpacingMd),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = goal.label,
                                style = MaterialTheme.typography.titleMedium,
                            )

                            IconButton(onClick = { viewModel.removeSavingsGoal(index) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                        ) {
                            OutlinedTextField(
                                value = monthlyTexts[index] ?: "",
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }
                                    monthlyTexts[index] = filtered
                                    viewModel.updateSavingsGoalMonthly(index, filtered)
                                },
                                label = { Text("Monthly") },
                                placeholder = { Text("0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                suffix = { Text(selectedCurrencyCode) },
                                modifier = Modifier.weight(1f),
                            )

                            OutlinedTextField(
                                value = targetTexts[index] ?: "",
                                onValueChange = { text ->
                                    val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }
                                    targetTexts[index] = filtered
                                    viewModel.updateSavingsGoalTarget(index, filtered)
                                },
                                label = { Text("Target (optional)") },
                                placeholder = { Text("0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                suffix = { Text(selectedCurrencyCode) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        if (goals.isEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
