package com.sofato.krone.ui.onboarding

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.R
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
        1 -> stringResource(R.string.onboarding_currency_title)
        2 -> stringResource(R.string.onboarding_income_title)
        3 -> stringResource(R.string.onboarding_expenses_title)
        4 -> stringResource(R.string.onboarding_savings_title)
        else -> stringResource(R.string.onboarding_result_title)
    }
    val stepSubtitle = when (currentStep) {
        1 -> stringResource(R.string.onboarding_currency_subtitle)
        2 -> stringResource(R.string.onboarding_income_subtitle)
        3 -> stringResource(R.string.onboarding_expenses_subtitle)
        4 -> stringResource(R.string.onboarding_savings_subtitle)
        else -> stringResource(R.string.onboarding_result_subtitle)
    }

    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingViewModel.OnboardingEvent.Completed -> onComplete()
                OnboardingViewModel.OnboardingEvent.ImportCompleted -> {
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Runtime.getRuntime().exit(0)
                }
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
                            text = stringResource(R.string.onboarding_step_counter, currentStep, viewModel.totalSteps - 1),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(
                            modifier = Modifier.width(88.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (currentStep in 3..4) {
                                TextButton(onClick = { viewModel.nextStep() }) {
                                    Text(stringResource(R.string.skip))
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
                        0 -> WelcomeStep(
                            onGetStarted = { viewModel.nextStep() },
                            onImportBackup = { importLauncher.launch(arrayOf("*/*")) },
                        )
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
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.back))
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
                            Text(stringResource(if (currentStep == 5) R.string.onboarding_get_started else R.string.next))
                        }
                    }
                }
            }
        }
    }
}
