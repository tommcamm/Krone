package com.sofato.krone.ui.onboarding.steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sofato.krone.ui.onboarding.OnboardingViewModel
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyIncomeStep(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val currencies by viewModel.enabledCurrencies.collectAsState()
    val selectedCode by viewModel.selectedCurrencyCode.collectAsState()
    val incomeDay by viewModel.incomeDay.collectAsState()

    var currencyExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    val selectedCurrency = currencies.find { it.code == selectedCode }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        // Currency selector
        Text(
            text = "Currency",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        ExposedDropdownMenuBox(
            expanded = currencyExpanded,
            onExpandedChange = { currencyExpanded = it },
        ) {
            OutlinedTextField(
                value = selectedCurrency?.let { "${it.code} - ${it.name} (${it.symbol})" } ?: selectedCode,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )

            ExposedDropdownMenu(
                expanded = currencyExpanded,
                onDismissRequest = { currencyExpanded = false },
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text("${currency.code} - ${currency.name} (${currency.symbol})") },
                        onClick = {
                            viewModel.onCurrencySelected(currency.code)
                            currencyExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingLg))

        // Income day picker
        Text(
            text = "Payday (day of month)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        ExposedDropdownMenuBox(
            expanded = dayExpanded,
            onExpandedChange = { dayExpanded = it },
        ) {
            OutlinedTextField(
                value = incomeDay.toString(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )

            ExposedDropdownMenu(
                expanded = dayExpanded,
                onDismissRequest = { dayExpanded = false },
            ) {
                (1..31).forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day.toString()) },
                        onClick = {
                            viewModel.onIncomeDayChanged(day)
                            dayExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}
