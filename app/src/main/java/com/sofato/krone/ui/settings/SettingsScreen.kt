package com.sofato.krone.ui.settings

import android.content.Intent
import android.net.Uri as AndroidUri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.R
import com.sofato.krone.ui.theme.Dimens
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SettingsScreen(
    onNavigateToCurrency: () -> Unit,
    onNavigateToCategories: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val darkModeOverride by viewModel.darkModeOverride.collectAsState()
    val isDynamicColorEnabled by viewModel.isDynamicColorEnabled.collectAsState()
    val showMonthlyCard by viewModel.showMonthlyCard.collectAsState()
    val showDailyCard by viewModel.showDailyCard.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportFailedMsg = stringResource(R.string.export_failed)
    val importSuccessMsg = stringResource(R.string.import_success)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        uri?.let { viewModel.exportDatabase(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { viewModel.importDatabase(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsViewModel.SettingsEvent.ExportSuccess -> {
                    snackbarHostState.showSnackbar(exportSuccessMsg)
                }
                is SettingsViewModel.SettingsEvent.ExportFailed -> {
                    snackbarHostState.showSnackbar("$exportFailedMsg: ${event.message}")
                }
                SettingsViewModel.SettingsEvent.ImportComplete -> {
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Runtime.getRuntime().exit(0)
                }
                SettingsViewModel.SettingsEvent.ResetComplete -> {
                    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Runtime.getRuntime().exit(0)
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            currentMode = darkModeOverride,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                viewModel.setDarkMode(mode)
                showThemeDialog = false
            },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.resetEverything()
                }) {
                    Text(
                        stringResource(R.string.settings_reset_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Appearance section
            item { SettingsSection(stringResource(R.string.settings_appearance)) }

            item {
                val themeLabel = when (darkModeOverride) {
                    "light" -> stringResource(R.string.settings_theme_light)
                    "dark" -> stringResource(R.string.settings_theme_dark)
                    else -> stringResource(R.string.settings_theme_system)
                }
                SettingsClickRow(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.settings_theme),
                    subtitle = themeLabel,
                    onClick = { showThemeDialog = true },
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.settings_dynamic_color),
                        subtitle = stringResource(R.string.settings_dynamic_color_subtitle),
                        checked = isDynamicColorEnabled,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                    )
                }
            }

            // Dashboard section
            item { SettingsSection("Dashboard") }

            item {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Dashboard,
                    title = "Monthly overview",
                    subtitle = "Show \"Left this month\" card",
                    checked = showMonthlyCard,
                    onCheckedChange = { viewModel.setShowMonthlyCard(it) },
                )
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Dashboard,
                    title = "Daily budget",
                    subtitle = "Show \"You can spend today\" card",
                    checked = showDailyCard,
                    onCheckedChange = { viewModel.setShowDailyCard(it) },
                )
            }

            // General section
            item { SettingsSection(stringResource(R.string.settings_general)) }

            item {
                SettingsClickRow(
                    icon = Icons.Outlined.CurrencyExchange,
                    title = stringResource(R.string.currency_settings),
                    onClick = onNavigateToCurrency,
                    showChevron = true,
                )
            }

            item {
                SettingsClickRow(
                    icon = Icons.Outlined.Category,
                    title = stringResource(R.string.categories),
                    onClick = onNavigateToCategories,
                    showChevron = true,
                )
            }

            // Data section
            item { SettingsSection(stringResource(R.string.settings_data)) }

            item {
                SettingsClickRow(
                    icon = Icons.Outlined.FileDownload,
                    title = stringResource(R.string.settings_export),
                    subtitle = stringResource(R.string.settings_export_subtitle),
                    onClick = {
                        val now = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                        exportLauncher.launch("krone-backup-$now.krone")
                    },
                )
            }

            item {
                SettingsClickRow(
                    icon = Icons.Outlined.FileUpload,
                    title = stringResource(R.string.settings_import),
                    subtitle = stringResource(R.string.settings_import_subtitle),
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                )
            }

            item {
                SettingsClickRow(
                    icon = Icons.Outlined.DeleteForever,
                    title = stringResource(R.string.settings_reset),
                    subtitle = stringResource(R.string.settings_reset_subtitle),
                    onClick = { showResetDialog = true },
                    titleColor = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.error,
                )
            }

            // About section
            item { SettingsSection(stringResource(R.string.settings_about)) }

            item {
                val versionName = remember {
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    } catch (_: Exception) {
                        "Unknown"
                    }
                }
                SettingsClickRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.settings_version),
                    subtitle = versionName,
                    onClick = {},
                )
            }

            item {
                SettingsClickRow(
                    icon = Icons.Outlined.Gavel,
                    title = stringResource(R.string.settings_license),
                    subtitle = stringResource(R.string.settings_license_subtitle),
                    onClick = {},
                )
            }

            item {
                SettingsClickRow(
                    icon = Icons.Outlined.OpenInNew,
                    title = stringResource(R.string.settings_source_code),
                    subtitle = stringResource(R.string.settings_source_code_subtitle),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, AndroidUri.parse("https://github.com/tommcamm/Krone"))
                        context.startActivity(intent)
                    },
                    showChevron = true,
                )
            }

            item { Spacer(Modifier.height(Dimens.SpacingLg)) }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start = Dimens.SpacingMd,
            end = Dimens.SpacingMd,
            top = Dimens.SpacingLg,
            bottom = Dimens.SpacingSm,
        ),
    )
}

@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showChevron: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.IconSizeMedium),
            tint = iconTint,
        )
        Spacer(Modifier.width(Dimens.SpacingMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showChevron) {
            Icon(
                Icons.AutoMirrored.Outlined.NavigateNext,
                contentDescription = null,
                modifier = Modifier.size(Dimens.IconSizeMedium),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.IconSizeMedium),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Dimens.SpacingMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ThemePickerDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val options = listOf(
        "system" to stringResource(R.string.settings_theme_system),
        "light" to stringResource(R.string.settings_theme_light),
        "dark" to stringResource(R.string.settings_theme_dark),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column {
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = Dimens.SpacingSm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                    ) {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = { onSelect(mode) },
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {},
    )
}
