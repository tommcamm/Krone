package com.sofato.krone.ui.expenses

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.R
import com.sofato.krone.domain.model.Category
import com.sofato.krone.ui.components.CategoryIcon
import com.sofato.krone.ui.components.CurrencyChip
import com.sofato.krone.ui.components.CurrencyPickerBottomSheet
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditExpenseScreen(
    onNavigateBack: () -> Unit,
    onManageCurrencies: () -> Unit,
    viewModel: EditExpenseViewModel = hiltViewModel(),
) {
    val amountInput by viewModel.amountInput.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val noteInput by viewModel.noteInput.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val enabledCurrencies by viewModel.enabledCurrencies.collectAsState()
    val convertedAmountText by viewModel.convertedAmountText.collectAsState()
    val isForeignCurrency by viewModel.isForeignCurrency.collectAsState()
    var showCurrencyPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditExpenseViewModel.EditExpenseEvent.Saved,
                EditExpenseViewModel.EditExpenseEvent.Deleted -> onNavigateBack()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.edit_expense)) },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                }
            },
            actions = {
                IconButton(onClick = viewModel::delete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            // Amount input with currency chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = viewModel::onAmountChanged,
                    label = { Text(stringResource(R.string.amount_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.headlineMedium,
                )
                Spacer(Modifier.width(Dimens.SpacingSm))
                selectedCurrency?.let { currency ->
                    CurrencyChip(
                        currencyCode = currency.code,
                        onClick = { showCurrencyPicker = true },
                    )
                }
            }

            // Conversion preview
            if (convertedAmountText != null) {
                Text(
                    text = convertedAmountText!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Category picker
            Text(
                text = stringResource(R.string.select_category),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            ) {
                categories.forEach { category ->
                    EditCategoryChipItem(
                        category = category,
                        isSelected = category.id == selectedCategory?.id,
                        onClick = { viewModel.onCategorySelected(category) },
                    )
                }
            }

            // Note input
            OutlinedTextField(
                value = noteInput,
                onValueChange = viewModel::onNoteChanged,
                label = { Text(stringResource(R.string.note_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimens.SpacingSm))

            // Save button
            Button(
                onClick = viewModel::save,
                enabled = amountInput.isNotBlank() && selectedCategory != null,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(stringResource(R.string.save))
            }

            // Delete button
            OutlinedButton(
                onClick = viewModel::delete,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.delete))
            }

            Spacer(Modifier.height(Dimens.SpacingXxl))
        }
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
}

@Composable
private fun EditCategoryChipItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primaryContainer
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .then(if (isSelected) Modifier.border(2.dp, bgColor, MaterialTheme.shapes.small) else Modifier)
            .padding(8.dp),
    ) {
        CategoryIcon(iconName = category.iconName, colorHex = category.colorHex, size = 48.dp, iconSize = 26.dp)
        Spacer(Modifier.height(4.dp))
        Text(text = category.name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}
