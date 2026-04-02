package com.sofato.krone.ui.onboarding.steps

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.sofato.krone.ui.onboarding.OnboardingResult
import com.sofato.krone.ui.onboarding.OnboardingViewModel
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun OnboardingResultStep(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
) {
    val result by viewModel.resultPreview.collectAsState()
    val currencies by viewModel.enabledCurrencies.collectAsState()
    val currency = currencies.find { it.code == result.currencyCode }

    // Spring-animated daily budget display
    val animatedDaily = remember { Animatable(0f) }
    LaunchedEffect(result.dailyBudgetMinor) {
        animatedDaily.animateTo(
            targetValue = result.dailyBudgetMinor.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpacingXl))

        Text(
            text = "Your daily budget",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingXl))

        // Hero daily amount
        val dailyDisplay = if (currency != null) {
            CurrencyFormatter.formatDisplay(animatedDaily.value.toLong(), currency)
        } else {
            "${animatedDaily.value.toLong() / 100} ${result.currencyCode}"
        }

        Text(
            text = dailyDisplay,
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "per day",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingXxl))

        // Breakdown card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier.padding(Dimens.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            ) {
                ResultRow(
                    label = "Income",
                    amountMinor = result.incomeMinor,
                    currencyCode = result.currencyCode,
                    currency = currency,
                )

                HorizontalDivider()

                ResultRow(
                    label = "Fixed costs",
                    amountMinor = -result.totalFixedMinor,
                    currencyCode = result.currencyCode,
                    currency = currency,
                )

                ResultRow(
                    label = "Savings",
                    amountMinor = -result.totalSavingsMinor,
                    currencyCode = result.currencyCode,
                    currency = currency,
                )

                HorizontalDivider()

                val discretionary = result.incomeMinor - result.totalFixedMinor - result.totalSavingsMinor
                ResultRow(
                    label = "Discretionary",
                    amountMinor = discretionary,
                    currencyCode = result.currencyCode,
                    currency = currency,
                    isBold = true,
                )
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    amountMinor: Long,
    currencyCode: String,
    currency: com.sofato.krone.domain.model.Currency?,
    isBold: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        )

        val display = if (currency != null) {
            val sign = if (amountMinor < 0) "-" else ""
            val absDisplay = CurrencyFormatter.formatDisplay(
                kotlin.math.abs(amountMinor),
                currency,
            )
            "$sign$absDisplay"
        } else {
            "${amountMinor / 100} $currencyCode"
        }

        Text(
            text = display,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
