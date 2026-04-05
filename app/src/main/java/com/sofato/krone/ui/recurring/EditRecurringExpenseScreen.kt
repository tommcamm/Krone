package com.sofato.krone.ui.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.sofato.krone.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditRecurringExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditRecurringExpenseViewModel = hiltViewModel(),
) {
    val label by viewModel.label.collectAsState()
    val amountInput by viewModel.amountInput.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val recurrenceRule by viewModel.recurrenceRule.collectAsState()
    val dayOfMonth by viewModel.dayOfMonth.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailedMessage = stringResource(R.string.error_save_failed)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditRecurringExpenseViewModel.Event.Saved,
                EditRecurringExpenseViewModel.Event.Deactivated -> onNavigateBack()
                EditRecurringExpenseViewModel.Event.Error -> {
                    snackbarHostState.showSnackbar(saveFailedMessage)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        TopAppBar(
            title = { Text("Edit recurring expense") },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = viewModel::deactivate) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Deactivate",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // Amount hero section
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.SpacingLg, horizontal = Dimens.SpacingMd),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BasicTextField(
                        value = amountInput,
                        onValueChange = viewModel::onAmountChanged,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontSize = MaterialTheme.typography.displayMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.Center) {
                                if (amountInput.isEmpty()) {
                                    Text(
                                        text = "0",
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingMd))

            // Name field
            OutlinedTextField(
                value = label,
                onValueChange = viewModel::onLabelChanged,
                label = { Text("Expense name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingMd),
            )

            Spacer(Modifier.height(Dimens.SpacingMd))

            // Frequency section
            Column(modifier = Modifier.padding(horizontal = Dimens.SpacingMd)) {
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                    FilterChip(
                        selected = recurrenceRule == RecurrenceRule.MONTHLY,
                        onClick = { viewModel.onRecurrenceRuleChanged(RecurrenceRule.MONTHLY) },
                        label = { Text("Monthly") },
                    )
                    FilterChip(
                        selected = recurrenceRule == RecurrenceRule.YEARLY,
                        onClick = { viewModel.onRecurrenceRuleChanged(RecurrenceRule.YEARLY) },
                        label = { Text("Yearly") },
                    )
                }
                if (recurrenceRule == RecurrenceRule.MONTHLY) {
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    ChargeDayInput(
                        dayOfMonth = dayOfMonth,
                        onDayChanged = viewModel::onDayOfMonthChanged,
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpacingMd))
            HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpacingMd))
            Spacer(Modifier.height(Dimens.SpacingMd))

            // Category section
            Column(modifier = Modifier.padding(horizontal = Dimens.SpacingMd)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                ) {
                    categories.forEach { category ->
                        CategoryPillChip(
                            category = category,
                            isSelected = category.id == selectedCategory?.id,
                            onClick = { viewModel.onCategorySelected(category) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingXl))
        }

        // Bottom action buttons
        Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
            Column(
                modifier = Modifier.padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            ) {
                Button(
                    onClick = viewModel::save,
                    enabled = label.isNotBlank() && amountInput.isNotBlank() && selectedCategory != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text("Save", style = MaterialTheme.typography.titleMedium)
                }
                OutlinedButton(
                    onClick = viewModel::deactivate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Deactivate")
                }
            }
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter),
    )
    }
}
