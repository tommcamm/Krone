package com.sofato.krone.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.sofato.krone.R
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter
import kotlinx.datetime.LocalDate
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseFilterSortBottomSheet(
    currentFilter: ExpenseFilter,
    currentSort: ExpenseSort,
    categories: List<Category>,
    homeCurrency: Currency?,
    onApply: (ExpenseFilter, ExpenseSort) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var draftFilter by remember { mutableStateOf(currentFilter) }
    var draftSort by remember { mutableStateOf(currentSort) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    var minAmountText by remember {
        mutableStateOf(
            currentFilter.minAmountMinor?.let {
                CurrencyFormatter.formatPlain(it, homeCurrency?.decimalPlaces ?: 2)
            } ?: "",
        )
    }
    var maxAmountText by remember {
        mutableStateOf(
            currentFilter.maxAmountMinor?.let {
                CurrencyFormatter.formatPlain(it, homeCurrency?.decimalPlaces ?: 2)
            } ?: "",
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.SpacingMd)
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.filter_and_sort),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Dimens.SpacingSm),
            )

            Column {
                SortSection(
                    selected = draftSort,
                    onSelected = { draftSort = it },
                )

                Spacer(Modifier.height(Dimens.SpacingMd))

                DateSection(
                    selected = draftFilter.dateRange,
                    onSelected = { draftFilter = draftFilter.copy(dateRange = it) },
                    onCustomClick = { showCustomDatePicker = true },
                )

                if (categories.isNotEmpty()) {
                    Spacer(Modifier.height(Dimens.SpacingMd))
                    CategorySection(
                        categories = categories,
                        selectedIds = draftFilter.categoryIds,
                        onToggle = { id ->
                            val next = if (id in draftFilter.categoryIds) {
                                draftFilter.categoryIds - id
                            } else {
                                draftFilter.categoryIds + id
                            }
                            draftFilter = draftFilter.copy(categoryIds = next)
                        },
                    )
                }

                Spacer(Modifier.height(Dimens.SpacingMd))

                SectionLabel(stringResource(R.string.filter_name))
                Spacer(Modifier.height(Dimens.SpacingXs))
                OutlinedTextField(
                    value = draftFilter.nameQuery,
                    onValueChange = { draftFilter = draftFilter.copy(nameQuery = it) },
                    placeholder = { Text(stringResource(R.string.filter_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Dimens.SpacingMd))

                AmountSection(
                    homeCurrency = homeCurrency,
                    minText = minAmountText,
                    maxText = maxAmountText,
                    onMinChange = { minAmountText = it },
                    onMaxChange = { maxAmountText = it },
                )

                Spacer(Modifier.height(Dimens.SpacingMd))
            }

            ActionRow(
                onClearAll = {
                    draftFilter = ExpenseFilter.Empty
                    draftSort = ExpenseSort.DateNewest
                    minAmountText = ""
                    maxAmountText = ""
                },
                onApply = {
                    val decimals = homeCurrency?.decimalPlaces ?: 2
                    val applied = draftFilter.copy(
                        minAmountMinor = minAmountText
                            .takeIf { it.isNotBlank() }
                            ?.let { CurrencyFormatter.parseToMinorUnits(it, decimals) },
                        maxAmountMinor = maxAmountText
                            .takeIf { it.isNotBlank() }
                            ?.let { CurrencyFormatter.parseToMinorUnits(it, decimals) },
                    )
                    onApply(applied, draftSort)
                },
            )

            Spacer(Modifier.height(Dimens.SpacingSm))
        }
    }

    if (showCustomDatePicker) {
        CustomDateRangeDialog(
            initial = draftFilter.dateRange as? DateRange.Custom,
            onDismiss = { showCustomDatePicker = false },
            onConfirm = { start, end ->
                draftFilter = draftFilter.copy(dateRange = DateRange.Custom(start, end))
                showCustomDatePicker = false
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SortSection(
    selected: ExpenseSort,
    onSelected: (ExpenseSort) -> Unit,
) {
    SectionLabel(stringResource(R.string.sort_by))
    Spacer(Modifier.height(Dimens.SpacingXs))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
    ) {
        SortOption(stringResource(R.string.sort_date_newest), selected == ExpenseSort.DateNewest) {
            onSelected(ExpenseSort.DateNewest)
        }
        SortOption(stringResource(R.string.sort_date_oldest), selected == ExpenseSort.DateOldest) {
            onSelected(ExpenseSort.DateOldest)
        }
        SortOption(stringResource(R.string.sort_amount_high), selected == ExpenseSort.AmountHigh) {
            onSelected(ExpenseSort.AmountHigh)
        }
        SortOption(stringResource(R.string.sort_amount_low), selected == ExpenseSort.AmountLow) {
            onSelected(ExpenseSort.AmountLow)
        }
    }
}

@Composable
private fun SortOption(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateSection(
    selected: DateRange,
    onSelected: (DateRange) -> Unit,
    onCustomClick: () -> Unit,
) {
    SectionLabel(stringResource(R.string.filter_date))
    Spacer(Modifier.height(Dimens.SpacingXs))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
    ) {
        DateChip(stringResource(R.string.filter_date_all_time), selected is DateRange.AllTime) {
            onSelected(DateRange.AllTime)
        }
        DateChip(stringResource(R.string.filter_date_this_month), selected is DateRange.ThisMonth) {
            onSelected(DateRange.ThisMonth)
        }
        DateChip(stringResource(R.string.filter_date_last_month), selected is DateRange.LastMonth) {
            onSelected(DateRange.LastMonth)
        }
        DateChip(stringResource(R.string.filter_date_last_3_months), selected is DateRange.Last3Months) {
            onSelected(DateRange.Last3Months)
        }
        DateChip(stringResource(R.string.filter_date_this_year), selected is DateRange.ThisYear) {
            onSelected(DateRange.ThisYear)
        }
        val customLabel = if (selected is DateRange.Custom) {
            stringResource(R.string.filter_date_custom_range, selected.start.toString(), selected.end.toString())
        } else {
            stringResource(R.string.filter_date_custom)
        }
        DateChip(customLabel, selected is DateRange.Custom, onClick = onCustomClick)
    }
}

@Composable
private fun DateChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    categories: List<Category>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
) {
    SectionLabel(stringResource(R.string.filter_category))
    Spacer(Modifier.height(Dimens.SpacingXs))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
    ) {
        categories.forEach { category ->
            val dotColor = try {
                Color(category.colorHex.toColorInt())
            } catch (_: Exception) {
                MaterialTheme.colorScheme.primary
            }
            FilterChip(
                selected = category.id in selectedIds,
                onClick = { onToggle(category.id) },
                label = { Text(category.name) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
private fun AmountSection(
    homeCurrency: Currency?,
    minText: String,
    maxText: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
) {
    val label = homeCurrency?.code?.let {
        stringResource(R.string.filter_amount_range, it)
    } ?: stringResource(R.string.filter_amount_range, "")
    SectionLabel(label)
    Spacer(Modifier.height(Dimens.SpacingXs))
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
    ) {
        OutlinedTextField(
            value = minText,
            onValueChange = onMinChange,
            placeholder = { Text(stringResource(R.string.filter_amount_min)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = maxText,
            onValueChange = onMaxChange,
            placeholder = { Text(stringResource(R.string.filter_amount_max)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ActionRow(
    onClearAll: () -> Unit,
    onApply: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onClearAll) {
            Text(stringResource(R.string.filter_clear_all))
        }
        Button(onClick = onApply) {
            Text(stringResource(R.string.filter_apply))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangeDialog(
    initial: DateRange.Custom?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initial?.start?.let { it.toEpochDays() * 86_400_000L },
        initialSelectedEndDateMillis = initial?.end?.let { it.toEpochDays() * 86_400_000L },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis = state.selectedStartDateMillis
                    val endMillis = state.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val start = LocalDate.fromEpochDays((startMillis / 86_400_000L).toInt())
                        val end = LocalDate.fromEpochDays((endMillis / 86_400_000L).toInt())
                        onConfirm(start, end)
                    }
                },
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
            ) {
                Text(stringResource(R.string.filter_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    ) {
        DateRangePicker(state = state)
    }
}
