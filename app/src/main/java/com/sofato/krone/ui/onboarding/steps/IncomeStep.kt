package com.sofato.krone.ui.onboarding.steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.sofato.krone.ui.onboarding.OnboardingViewModel
import com.sofato.krone.ui.theme.Dimens

@Composable
fun IncomeStep(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val incomeAmount by viewModel.incomeAmount.collectAsState()
    val incomeLabel by viewModel.incomeLabel.collectAsState()
    val selectedCurrencyCode by viewModel.selectedCurrencyCode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        // Income label
        Text(
            text = "Label",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        OutlinedTextField(
            value = incomeLabel,
            onValueChange = { viewModel.onIncomeLabelChanged(it) },
            placeholder = { Text("e.g. Salary") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingLg))

        // Income amount
        Text(
            text = "Amount ($selectedCurrencyCode)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        OutlinedTextField(
            value = incomeAmount,
            onValueChange = { viewModel.onIncomeAmountChanged(it) },
            placeholder = { Text("0.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
