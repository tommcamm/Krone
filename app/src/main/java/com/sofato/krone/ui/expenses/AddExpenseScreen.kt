package com.sofato.krone.ui.expenses

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.R
import com.sofato.krone.domain.model.Category
import com.sofato.krone.ui.components.CategoryIcon
import com.sofato.krone.ui.components.CurrencyChip
import com.sofato.krone.ui.components.CurrencyPickerBottomSheet
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.today
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onManageCategories: () -> Unit,
    onManageCurrencies: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel(),
) {
    val amountInput by viewModel.amountInput.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val noteInput by viewModel.noteInput.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val enabledCurrencies by viewModel.enabledCurrencies.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val convertedAmountText by viewModel.convertedAmountText.collectAsState()
    val rateFreshness by viewModel.rateFreshness.collectAsState()
    val isForeignCurrency by viewModel.isForeignCurrency.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val focusRequester = remember { FocusRequester() }
    val rateUnavailableMessage = stringResource(R.string.error_rate_unavailable)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddExpenseViewModel.AddExpenseEvent.Saved -> onNavigateBack()
                AddExpenseViewModel.AddExpenseEvent.RateUnavailable -> {
                    snackbarHostState.showSnackbar(rateUnavailableMessage)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        TopAppBar(
            title = { Text(stringResource(R.string.add_expense)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                }
            },
            actions = {
                IconButton(
                    onClick = viewModel::save,
                    enabled = amountInput.isNotBlank() && !isSaving,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.save),
                        tint = if (amountInput.isNotBlank() && !isSaving) {
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
                selectedCurrency?.let { currency ->
                    CurrencyChip(
                        currencyCode = currency.code,
                        onClick = { showCurrencyPicker = true },
                    )
                }
                Spacer(Modifier.height(Dimens.SpacingMd))
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
                if (convertedAmountText != null) {
                    Spacer(Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = convertedAmountText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (isForeignCurrency) {
                    Spacer(Modifier.height(Dimens.SpacingXs))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val dotColor = when (rateFreshness) {
                            RateFreshness.FRESH -> MaterialTheme.colorScheme.tertiary
                            RateFreshness.STALE -> Color(0xFFF59E0B)
                            RateFreshness.UNAVAILABLE -> MaterialTheme.colorScheme.error
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(dotColor, CircleShape),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = when (rateFreshness) {
                                RateFreshness.FRESH -> stringResource(R.string.rate_fresh)
                                RateFreshness.STALE -> stringResource(R.string.rate_stale)
                                RateFreshness.UNAVAILABLE -> stringResource(R.string.rates_unavailable)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpacingMd))
            Spacer(Modifier.height(Dimens.SpacingMd))

            // Category section — compact chips
            Column(modifier = Modifier.padding(horizontal = Dimens.SpacingMd)) {
                Text(
                    text = stringResource(R.string.select_category),
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

            Spacer(Modifier.height(Dimens.SpacingMd))
            HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpacingMd))
            Spacer(Modifier.height(Dimens.SpacingMd))

            // Note input
            OutlinedTextField(
                value = noteInput,
                onValueChange = viewModel::onNoteChanged,
                label = { Text(stringResource(R.string.note_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingMd),
            )

            Spacer(Modifier.height(Dimens.SpacingMd))

            // Date picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(horizontal = Dimens.SpacingMd, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (selectedDate == LocalDate.today()) {
                        stringResource(R.string.today)
                    } else {
                        selectedDate.toString()
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(Dimens.SpacingXl))
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter),
    )
    }

    if (showCurrencyPicker) {
        CurrencyPickerBottomSheet(
            currencies = enabledCurrencies,
            selectedCode = selectedCurrency?.code ?: "",
            onCurrencySelected = {
                viewModel.onCurrencySelected(it)
                showCurrencyPicker = false
            },
            onDismiss = { showCurrencyPicker = false },
            onManageCurrencies = onManageCurrencies,
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
internal fun CompactCategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primaryContainer
    }

    val shape = MaterialTheme.shapes.small

    Box(
        modifier = Modifier
            .clip(shape)
            .then(
                if (isSelected) Modifier.border(1.5.dp, bgColor, shape)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            )
            .background(if (isSelected) bgColor.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CategoryIcon(
                iconName = category.iconName,
                colorHex = category.colorHex,
                size = 24.dp,
                iconSize = 14.dp,
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) bgColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
