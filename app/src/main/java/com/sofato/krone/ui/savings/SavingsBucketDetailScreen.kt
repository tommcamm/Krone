package com.sofato.krone.ui.savings

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.ui.savings.components.SavingsProgressArc
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter
import kotlin.math.pow
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsBucketDetailScreen(
    onNavigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: SavingsBucketDetailViewModel = hiltViewModel(),
) {
    val bucket by viewModel.bucket.collectAsState()
    val contributions by viewModel.contributions.collectAsState()
    val currency by viewModel.homeCurrency.collectAsState()
    var showBalanceDialog by remember { mutableStateOf(false) }

    val b = bucket ?: return
    val curr = currency ?: return

    val hasTarget = b.targetAmountMinor != null && b.targetAmountMinor > 0
    val progress = if (hasTarget) {
        (b.currentBalanceMinor.toFloat() / b.targetAmountMinor!!).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(b.label) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { onEdit(b.id) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            item { Spacer(Modifier.height(Dimens.SpacingXs)) }

            // Info card
            item(key = "info") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation),
                ) {
                    Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
                        if (hasTarget) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                            ) {
                                SavingsProgressArc(
                                    progress = progress,
                                    size = 64.dp,
                                    strokeWidth = 6.dp,
                                )
                                Column {
                                    Text(
                                        text = CurrencyFormatter.formatDisplay(b.currentBalanceMinor, curr),
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                    Text(
                                        text = "of ${CurrencyFormatter.formatDisplay(b.targetAmountMinor!!, curr)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Balance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = CurrencyFormatter.formatDisplay(b.currentBalanceMinor, curr),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }

                        Spacer(Modifier.height(Dimens.SpacingSm))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Monthly contribution",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = CurrencyFormatter.formatDisplay(b.monthlyContributionMinor, curr),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Type",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = b.type.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            // Update balance button
            item(key = "update_balance") {
                TextButton(onClick = { showBalanceDialog = true }) {
                    Text("Update balance")
                }
            }

            // Contributions header
            if (contributions.isNotEmpty()) {
                item(key = "contributions_header") {
                    Text(
                        text = "Contribution history",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                items(items = contributions, key = { it.id }) { contribution ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = contribution.date.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (contribution.isAutoPosted) {
                                Text(
                                    text = "Auto-posted",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = if (contribution.isSkipped) "Skipped"
                            else CurrencyFormatter.formatDisplay(contribution.amountMinor, curr),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (contribution.isSkipped) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    HorizontalDivider()
                }
            }

            item { Spacer(Modifier.height(Dimens.SpacingXxl)) }
        }
    }

    if (showBalanceDialog) {
        var balanceInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBalanceDialog = false },
            title = { Text("Update balance") },
            text = {
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { balanceInput = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("New balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = balanceInput.replace(",", ".").toDoubleOrNull()
                        if (parsed != null && parsed >= 0) {
                            val minor = (parsed * 10.0.pow(curr.decimalPlaces)).roundToLong()
                            viewModel.updateBalance(minor)
                            showBalanceDialog = false
                        }
                    },
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBalanceDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
