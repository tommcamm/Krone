package com.sofato.krone.ui.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.sofato.krone.ui.components.CategoryIcon
import com.sofato.krone.ui.onboarding.OnboardingViewModel
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun FixedExpensesStep(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val expenses by viewModel.fixedExpenses.collectAsState()
    val selectedCurrencyCode by viewModel.selectedCurrencyCode.collectAsState()

    // Local text state for each row to allow free-form editing
    val textFields = remember {
        mutableStateMapOf<Int, String>().apply {
            expenses.forEachIndexed { index, expense ->
                put(index, if (expense.amountMinor == 0L) "" else CurrencyFormatter.formatPlain(expense.amountMinor, 2))
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Text(
            text = "Fixed monthly expenses",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        Text(
            text = "Fill in the amounts you pay each month. Leave blank to skip.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        ) {
            itemsIndexed(expenses, key = { index, _ -> index }) { index, expense ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryIcon(
                        iconName = expense.iconName,
                        colorHex = expense.colorHex,
                    )

                    Spacer(modifier = Modifier.width(Dimens.SpacingMd))

                    Text(
                        text = expense.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(modifier = Modifier.width(Dimens.SpacingSm))

                    OutlinedTextField(
                        value = textFields[index] ?: "",
                        onValueChange = { text ->
                            val filtered = text.filter { it.isDigit() || it == '.' || it == ',' }
                            textFields[index] = filtered
                            viewModel.updateFixedExpenseAmount(index, filtered)
                        },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        suffix = { Text(selectedCurrencyCode) },
                        modifier = Modifier.width(140.dp),
                    )
                }
            }
        }
    }
}
