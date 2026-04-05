package com.sofato.krone.ui.savings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.sofato.krone.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.domain.model.SavingsBucketType
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSavingsBucketScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSavingsBucketViewModel = hiltViewModel(),
) {
    val label by viewModel.label.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val monthlyInput by viewModel.monthlyContributionInput.collectAsState()
    val targetInput by viewModel.targetAmountInput.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailedMessage = stringResource(R.string.error_save_failed)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddSavingsBucketViewModel.Event.Saved -> onNavigateBack()
                AddSavingsBucketViewModel.Event.Error -> {
                    snackbarHostState.showSnackbar(saveFailedMessage)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        TopAppBar(
            title = { Text("Add savings bucket") },
            windowInsets = WindowInsets(0, 0, 0, 0),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // Amount hero section
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.SpacingLg, horizontal = Dimens.SpacingMd),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Monthly contribution",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    BasicTextField(
                        value = monthlyInput,
                        onValueChange = viewModel::onMonthlyContributionChanged,
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
                                if (monthlyInput.isEmpty()) {
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
                }
            }

            Spacer(Modifier.height(Dimens.SpacingMd))

            // Label field
            OutlinedTextField(
                value = label,
                onValueChange = viewModel::onLabelChanged,
                label = { Text("Bucket name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingMd),
            )

            Spacer(Modifier.height(Dimens.SpacingMd))

            // Target amount (optional)
            OutlinedTextField(
                value = targetInput,
                onValueChange = viewModel::onTargetAmountChanged,
                label = { Text("Target amount (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingMd),
            )

            Spacer(Modifier.height(Dimens.SpacingMd))
            HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpacingMd))
            Spacer(Modifier.height(Dimens.SpacingMd))

            // Type section
            Column(modifier = Modifier.padding(horizontal = Dimens.SpacingMd)) {
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                ) {
                    SavingsBucketType.entries.forEach { type ->
                        FilterChip(
                            selected = type == selectedType,
                            onClick = { viewModel.onTypeSelected(type) },
                            label = { Text(type.displayName) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.SpacingXl))
        }

        // Bottom save button
        Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
            Button(
                onClick = viewModel::save,
                enabled = label.isNotBlank() && monthlyInput.isNotBlank() && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Save", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter),
    )
    }
}
