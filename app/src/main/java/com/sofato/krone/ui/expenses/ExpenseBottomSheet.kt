package com.sofato.krone.ui.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sofato.krone.R
import com.sofato.krone.ui.components.CompactCategoryChip
import com.sofato.krone.ui.components.CurrencyChip
import com.sofato.krone.ui.components.CurrencyPickerDialog
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.today
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBottomSheet(
    viewModel: ExpenseSheetViewModel,
    onDismiss: () -> Unit,
) {
    val calculatorState by viewModel.calculatorState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val noteInput by viewModel.noteInput.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val enabledCurrencies by viewModel.enabledCurrencies.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val convertedAmountText by viewModel.convertedAmountText.collectAsState()
    val rateFreshness by viewModel.rateFreshness.collectAsState()
    val isForeignCurrency by viewModel.isForeignCurrency.collectAsState()

    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNoteField by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val rateUnavailableMessage = stringResource(R.string.error_rate_unavailable)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ExpenseSheetViewModel.ExpenseSheetEvent.Saved,
                ExpenseSheetViewModel.ExpenseSheetEvent.Deleted -> onDismiss()
                ExpenseSheetViewModel.ExpenseSheetEvent.RateUnavailable -> {
                    snackbarHostState.showSnackbar(rateUnavailableMessage)
                }
            }
        }
    }

    // Show note field if editing an expense that has a note
    LaunchedEffect(isEditMode) {
        if (isEditMode && noteInput.isNotBlank()) {
            showNoteField = true
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingMd)
                    .navigationBarsPadding(),
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(if (isEditMode) R.string.edit_expense else R.string.new_expense),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (isEditMode) {
                            IconButton(onClick = viewModel::delete) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        selectedCurrency?.let { currency ->
                            CurrencyChip(
                                currencyCode = currency.code,
                                onClick = { showCurrencyPicker = true },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.SpacingSm))

                // Category chips — horizontal scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(categories, key = { it.id }) { category ->
                        CompactCategoryChip(
                            category = category,
                            isSelected = category.id == selectedCategory?.id,
                            onClick = { viewModel.onCategorySelected(category) },
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.SpacingMd))

                // Amount display with currency symbol and backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = Dimens.SpacingSm),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = calculatorState.displayText,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-2).sp,
                            ),
                            maxLines = 1,
                            color = if (calculatorState.hasError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                        selectedCurrency?.let { currency ->
                            Text(
                                text = " ${currency.symbol}",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-1).sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                maxLines = 1,
                            )
                        }
                    }
                    if (calculatorState.expression.isNotEmpty()) {
                        val backspaceDescription = stringResource(R.string.cd_backspace)
                        Text(
                            text = "\u232B",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable(onClick = viewModel::onBackspace)
                                .semantics {
                                    contentDescription = backspaceDescription
                                    role = Role.Button
                                }
                                .padding(Dimens.SpacingSm),
                        )
                    }
                }

                // Expression sub-line (show expression when result is displayed)
                if (calculatorState.result != null && calculatorState.expression.isNotEmpty()) {
                    Text(
                        text = calculatorState.expression
                            .replace('*', '\u00D7')
                            .replace('/', '\u00F7'),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingSm),
                    )
                }

                Spacer(Modifier.height(Dimens.SpacingSm))

                // Metadata row: date, note toggle, conversion preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                ) {
                    // Date chip
                    FilledTonalButton(
                        onClick = { showDatePicker = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (selectedDate == LocalDate.today()) {
                                stringResource(R.string.today)
                            } else {
                                selectedDate.toString()
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    // Note toggle
                    FilledTonalButton(
                        onClick = { showNoteField = !showNoteField },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.note_hint),
                            modifier = Modifier.size(16.dp),
                        )
                        if (noteInput.isNotBlank()) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = noteInput,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Conversion preview + freshness
                    if (isForeignCurrency) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (convertedAmountText != null) {
                                Text(
                                    text = convertedAmountText!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            val dotColor = when (rateFreshness) {
                                RateFreshness.FRESH -> MaterialTheme.colorScheme.tertiary
                                RateFreshness.STALE -> Color(0xFFF59E0B)
                                RateFreshness.UNAVAILABLE -> MaterialTheme.colorScheme.error
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(dotColor, CircleShape),
                            )
                        }
                    }
                }

                // Expandable note field
                AnimatedVisibility(visible = showNoteField) {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = viewModel::onNoteChanged,
                        label = { Text(stringResource(R.string.note_hint)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.SpacingSm),
                    )
                }

                Spacer(Modifier.height(Dimens.SpacingMd))

                // Calculator keypad
                CalculatorKeypad(
                    onDigit = viewModel::onDigit,
                    onDecimal = viewModel::onDecimal,
                    onOperator = viewModel::onOperator,
                    onAction = viewModel::save,
                    actionLabel = stringResource(if (isEditMode) R.string.save else R.string.action_add),
                    isActionEnabled = calculatorState.expression.isNotEmpty() && !isSaving,
                )

                Spacer(Modifier.height(Dimens.SpacingSm))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currencies = enabledCurrencies,
            selectedCode = selectedCurrency?.code ?: "",
            onCurrencySelected = {
                viewModel.onCurrencySelected(it)
                showCurrencyPicker = false
            },
            onDismiss = { showCurrencyPicker = false },
        )
    }

    if (showDatePicker) {
        val today = LocalDate.today()
        val todayMillis = today.toEpochDays() * 86_400_000L
        val selectedMillis = selectedDate.toEpochDays() * 86_400_000L
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= todayMillis
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val epochDays = (millis / 86_400_000L).toInt()
                        val date = LocalDate.fromEpochDays(epochDays)
                        viewModel.onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.close))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CalculatorKeypad(
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onAction: () -> Unit,
    actionLabel: String,
    isActionEnabled: Boolean,
) {
    val buttonShape = RoundedCornerShape(16.dp)
    val buttonModifier = Modifier
        .height(56.dp)
        .fillMaxWidth()

    val rows = listOf(
        listOf(KeyDef.Op('\u00F7'), KeyDef.Digit('7'), KeyDef.Digit('8'), KeyDef.Digit('9')),
        listOf(KeyDef.Op('\u00D7'), KeyDef.Digit('4'), KeyDef.Digit('5'), KeyDef.Digit('6')),
        listOf(KeyDef.Op('\u2212'), KeyDef.Digit('1'), KeyDef.Digit('2'), KeyDef.Digit('3')),
        listOf(KeyDef.Op('+'), KeyDef.Decimal, KeyDef.Digit('0'), KeyDef.Action),
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row.forEach { key ->
                    when (key) {
                        is KeyDef.Digit -> {
                            FilledTonalButton(
                                onClick = { onDigit(key.char) },
                                modifier = buttonModifier.weight(1f),
                                shape = buttonShape,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    text = key.char.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        is KeyDef.Op -> {
                            val opDescription = stringResource(
                                when (key.char) {
                                    '\u00F7' -> R.string.cd_op_divide
                                    '\u00D7' -> R.string.cd_op_multiply
                                    '\u2212' -> R.string.cd_op_minus
                                    else -> R.string.cd_op_plus
                                }
                            )
                            FilledTonalButton(
                                onClick = { onOperator(key.char) },
                                modifier = buttonModifier
                                    .weight(1f)
                                    .semantics { contentDescription = opDescription },
                                shape = buttonShape,
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                            ) {
                                Text(
                                    text = key.char.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        KeyDef.Decimal -> {
                            val decimalDescription = stringResource(R.string.cd_decimal_point)
                            FilledTonalButton(
                                onClick = onDecimal,
                                modifier = buttonModifier
                                    .weight(1f)
                                    .semantics { contentDescription = decimalDescription },
                                shape = buttonShape,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    text = ".",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        KeyDef.Action -> {
                            Button(
                                onClick = onAction,
                                enabled = isActionEnabled,
                                modifier = buttonModifier.weight(1f),
                                shape = buttonShape,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    text = actionLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed interface KeyDef {
    data class Digit(val char: Char) : KeyDef
    data class Op(val char: Char) : KeyDef
    data object Decimal : KeyDef
    data object Action : KeyDef
}
