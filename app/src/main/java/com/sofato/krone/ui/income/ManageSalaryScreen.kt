package com.sofato.krone.ui.income

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSalaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageSalaryViewModel = hiltViewModel(),
) {
    val amountInput by viewModel.amountInput.collectAsState()
    val labelInput by viewModel.labelInput.collectAsState()
    val incomeDay by viewModel.incomeDay.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val editingIncome by viewModel.editingIncome.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailedMessage = stringResource(R.string.error_save_failed)

    LaunchedEffect(editingIncome) {
        if (editingIncome != null) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ManageSalaryViewModel.Event.Saved -> onNavigateBack()
                ManageSalaryViewModel.Event.Error -> {
                    snackbarHostState.showSnackbar(saveFailedMessage)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        TopAppBar(
            title = { Text("Manage salary") },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

        if (editingIncome == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No income set up yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
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
                        Text(
                            text = "Monthly net income",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(Dimens.SpacingSm))
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
                }

                Spacer(Modifier.height(Dimens.SpacingMd))

                // Label field
                OutlinedTextField(
                    value = labelInput,
                    onValueChange = viewModel::onLabelChanged,
                    label = { Text("Income label") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingMd),
                )

                Spacer(Modifier.height(Dimens.SpacingMd))
                HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpacingMd))
                Spacer(Modifier.height(Dimens.SpacingMd))

                // Payday section
                Column(modifier = Modifier.padding(horizontal = Dimens.SpacingMd)) {
                    Text(
                        text = "Payday",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    PaydayInput(
                        dayOfMonth = incomeDay,
                        onDayChanged = viewModel::onIncomeDayChanged,
                    )
                    Spacer(Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = "Your budget period resets on this day each month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(Dimens.SpacingXl))
            }

            // Bottom save button
            Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
                Button(
                    onClick = viewModel::save,
                    enabled = labelInput.isNotBlank() && amountInput.isNotBlank() && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text("Save", style = MaterialTheme.typography.titleMedium)
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

@Composable
private fun PaydayInput(
    dayOfMonth: Int,
    onDayChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(dayOfMonth) {
        mutableStateOf(dayOfMonth.toString())
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Paid on the",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(2)
                text = filtered
                val parsed = filtered.toIntOrNull()
                if (parsed != null) onDayChanged(parsed.coerceIn(1, 31))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(64.dp),
        )
        Text(
            text = "of each month",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
