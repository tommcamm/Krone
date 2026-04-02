package com.sofato.krone.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.ui.onboarding.steps.CurrencyIncomeStep
import com.sofato.krone.ui.onboarding.steps.FixedExpensesStep
import com.sofato.krone.ui.onboarding.steps.IncomeStep
import com.sofato.krone.ui.onboarding.steps.OnboardingResultStep
import com.sofato.krone.ui.onboarding.steps.SavingsGoalsStep
import com.sofato.krone.ui.onboarding.steps.WelcomeStep
import com.sofato.krone.ui.theme.Dimens

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isWelcomeStep = currentStep == 0
    val stepTitle = when (currentStep) {
        1 -> "Currency and payday"
        2 -> "Income"
        3 -> "Fixed commitments"
        4 -> "Savings goals"
        else -> "Your plan"
    }
    val stepSubtitle = when (currentStep) {
        1 -> "Set your baseline and salary date."
        2 -> "Tell us how much comes in each month."
        3 -> "Add monthly and yearly fixed costs."
        4 -> "Choose what to save automatically."
        else -> "Review your daily budget before finishing."
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingViewModel.OnboardingEvent.Completed -> onComplete()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(Dimens.SpacingLg),
            ) {
                if (!isWelcomeStep) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.MinTouchTarget),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Step $currentStep of ${viewModel.totalSteps - 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(
                            modifier = Modifier.width(88.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (currentStep in 3..4) {
                                TextButton(onClick = { viewModel.nextStep() }) {
                                    Text("Skip")
                                }
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { currentStep.toFloat() / (viewModel.totalSteps - 1).toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpacingMd))

                    Text(
                        text = stepTitle,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                    Text(
                        text = stepSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpacingMd))
                }

                AnimatedContent(
                    targetState = currentStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "onboarding_step",
                ) { step ->
                    when (step) {
                        0 -> WelcomeStep(onGetStarted = { viewModel.nextStep() })
                        1 -> CurrencyIncomeStep(viewModel = viewModel)
                        2 -> IncomeStep(viewModel = viewModel)
                        3 -> FixedExpensesStep(viewModel = viewModel)
                        4 -> SavingsGoalsStep(viewModel = viewModel)
                        5 -> OnboardingResultStep(viewModel = viewModel)
                    }
                }

                if (!isWelcomeStep) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingMd))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { viewModel.previousStep() },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Back")
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = {
                                if (currentStep == 5) {
                                    viewModel.complete()
                                } else {
                                    viewModel.nextStep()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (currentStep == 5) "Get started" else "Next")
                        }
                    }
                }
            }
        }
    }
}
