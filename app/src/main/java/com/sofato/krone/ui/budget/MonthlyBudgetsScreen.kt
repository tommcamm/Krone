package com.sofato.krone.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sofato.krone.domain.model.Category
import com.sofato.krone.ui.components.CategoryIcon
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter
import kotlin.math.pow
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyBudgetsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MonthlyBudgetsViewModel = hiltViewModel(),
) {
    val budgetItems by viewModel.budgetItems.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val currency by viewModel.homeCurrency.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    val curr = currency ?: return

    var showCategoryPicker by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CategoryBudgetItem?>(null) }
    var addingCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Budgets") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            if (availableCategories.isNotEmpty()) {
                FloatingActionButton(onClick = { showCategoryPicker = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add category budget")
                }
            }
        },
    ) { innerPadding ->
        if (budgetItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No category budgets set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    Text(
                        text = "Tap + to set a monthly limit for a category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(items = budgetItems, key = { it.allocation.id }) { item ->
                    SwipeToDismissBudgetItem(
                        item = item,
                        formattedAmount = CurrencyFormatter.formatDisplay(item.allocation.allocatedAmountMinor, curr),
                        onDismiss = { viewModel.deleteAllocation(item.allocation.id) },
                        onClick = { editingItem = item },
                        modifier = Modifier.animateItem(),
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = Dimens.SpacingMd))
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }

    // Category picker bottom sheet
    if (showCategoryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryPicker = false },
            sheetState = rememberModalBottomSheetState(),
        ) {
            Text(
                text = "Choose a category",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm),
            )
            LazyColumn {
                items(items = availableCategories, key = { it.id }) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCategoryPicker = false
                                addingCategory = category
                            }
                            .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm + Dimens.SpacingXs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryIcon(
                            iconName = category.iconName,
                            colorHex = category.colorHex,
                        )
                        Spacer(Modifier.width(Dimens.SpacingSm))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                item { Spacer(Modifier.height(Dimens.SpacingXxl)) }
            }
        }
    }

    // Edit existing budget dialog
    editingItem?.let { item ->
        val initialValue = run {
            val display = item.allocation.allocatedAmountMinor.toDouble() / 10.0.pow(curr.decimalPlaces)
            if (display == display.toLong().toDouble()) display.toLong().toString() else display.toString()
        }
        var amountInput by remember(item.allocation.id) { mutableStateOf(initialValue) }
        AmountDialog(
            title = "Budget for ${item.category.name}",
            amountInput = amountInput,
            onAmountChange = { amountInput = it },
            currency = curr,
            onConfirm = { minor ->
                viewModel.saveAllocation(item.category.id, minor)
                editingItem = null
            },
            onDismiss = { editingItem = null },
        )
    }

    // Add new budget dialog
    addingCategory?.let { category ->
        var amountInput by remember(category.id) { mutableStateOf("") }
        AmountDialog(
            title = "Budget for ${category.name}",
            amountInput = amountInput,
            onAmountChange = { amountInput = it },
            currency = curr,
            onConfirm = { minor ->
                viewModel.saveAllocation(category.id, minor)
                addingCategory = null
            },
            onDismiss = { addingCategory = null },
        )
    }
}

@Composable
private fun AmountDialog(
    title: String,
    amountInput: String,
    onAmountChange: (String) -> Unit,
    currency: com.sofato.krone.domain.model.Currency,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = amountInput,
                onValueChange = { onAmountChange(it.filter { c -> c.isDigit() || c == '.' || c == ',' }) },
                label = { Text("Monthly limit (${currency.code})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = amountInput.replace(",", ".").toDoubleOrNull()
                    if (parsed != null && parsed > 0) {
                        val minor = (parsed * 10.0.pow(currency.decimalPlaces)).roundToLong()
                        onConfirm(minor)
                    }
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissBudgetItem(
    item: CategoryBudgetItem,
    formattedAmount: String,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm + Dimens.SpacingXs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(
                        iconName = item.category.iconName,
                        colorHex = item.category.colorHex,
                    )
                    Spacer(Modifier.width(Dimens.SpacingSm))
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}
