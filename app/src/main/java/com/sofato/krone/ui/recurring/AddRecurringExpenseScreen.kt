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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.domain.model.RecurrenceRule
import com.sofato.krone.ui.components.CurrencyChip
import com.sofato.krone.ui.components.CompactCategoryChip
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecurringExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddRecurringExpenseViewModel = hiltViewModel(),
) {
    val label by viewModel.label.collectAsState()
    val amountInput by viewModel.amountInput.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val recurrenceRule by viewModel.recurrenceRule.collectAsState()
    val dayOfMonth by viewModel.dayOfMonth.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailedMessage = stringResource(R.string.error_save_failed)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddRecurringExpenseViewModel.Event.Saved -> onNavigateBack()
                AddRecurringExpenseViewModel.Event.Error -> {
                    snackbarHostState.showSnackbar(saveFailedMessage)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        TopAppBar(
            title = { Text("Add recurring expense") },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = viewModel::save,
                    enabled = label.isNotBlank() && amountInput.isNotBlank() && selectedCategory != null && !isSaving,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.save),
                        tint = if (label.isNotBlank() && amountInput.isNotBlank() && selectedCategory != null && !isSaving) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        },
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.SpacingLg, horizontal = Dimens.SpacingMd),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (currencyCode.isNotEmpty()) {
                    CurrencyChip(
                        currencyCode = currencyCode,
                        onClick = { },
                    )
                    Spacer(Modifier.height(Dimens.SpacingMd))
                }
                BasicTextField(
                    value = amountInput,
                    onValueChange = viewModel::onAmountChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
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

            HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpacingMd))
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    categories.forEach { category ->
                        CompactCategoryChip(
                            category = category,
                            isSelected = category.id == selectedCategory?.id,
                            onClick = { viewModel.onCategorySelected(category) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingXl))
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter),
    )
    }
}
