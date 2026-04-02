package com.sofato.krone.ui.onboarding.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.sofato.krone.ui.components.CategoryIcon
import com.sofato.krone.ui.onboarding.OnboardingViewModel
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter
import kotlin.math.max

@Composable
fun FixedExpensesStep(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val expenses by viewModel.fixedExpenses.collectAsState()
    val selectedCurrencyCode by viewModel.selectedCurrencyCode.collectAsState()
    val monthlyEquivalentMinor = expenses.sumOf { expense ->
        if (expense.recurrenceRule == "YEARLY") max(0L, expense.amountMinor / 12L) else expense.amountMinor
    }

    val textFields = remember {
        mutableStateMapOf<Int, String>().apply {
            expenses.forEachIndexed { index, expense ->
                put(index, if (expense.amountMinor == 0L) "" else CurrencyFormatter.formatPlain(expense.amountMinor, 2))
            }
        }
    }

    var expandedIndex by remember { mutableStateOf(-1) }

    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingMd)) {
                Text(
                    text = "Estimated fixed load per month",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${CurrencyFormatter.formatPlain(monthlyEquivalentMinor, 2)} $selectedCurrencyCode",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                Text(
                    text = "Yearly commitments are divided by 12 for this preview.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            itemsIndexed(expenses, key = { index, _ -> index }) { index, expense ->
                val isExpanded = expandedIndex == index
                val amountText = textFields[index] ?: ""
                val hasAmount = expense.amountMinor > 0L

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedIndex = if (isExpanded) -1 else index
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    ),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(Dimens.SpacingSm + Dimens.SpacingXs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryIcon(
                                iconName = expense.iconName,
                                colorHex = expense.colorHex,
                                size = Dimens.IconSizeLarge,
                                iconSize = Dimens.IconSizeSmall,
                            )
                            Text(
                                text = expense.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            if (!isExpanded) {
                                Text(
                                    text = if (hasAmount) {
                                        "${CurrencyFormatter.formatPlain(expense.amountMinor, 2)} $selectedCurrencyCode"
                                    } else {
                                        "Tap to set"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (hasAmount) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(Dimens.SpacingSm))
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { text ->
                                        val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }
                                        textFields[index] = filtered
                                        viewModel.updateFixedExpenseAmount(index, filtered)
                                    },
                                    label = { Text("Amount") },
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    suffix = { Text(selectedCurrencyCode) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(modifier = Modifier.height(Dimens.SpacingSm))
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                                    FilterChip(
                                        selected = expense.recurrenceRule == "MONTHLY",
                                        onClick = { viewModel.updateFixedExpenseRecurrence(index, "MONTHLY") },
                                        label = { Text("Monthly") },
                                    )
                                    FilterChip(
                                        selected = expense.recurrenceRule == "YEARLY",
                                        onClick = { viewModel.updateFixedExpenseRecurrence(index, "YEARLY") },
                                        label = { Text("Yearly") },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
