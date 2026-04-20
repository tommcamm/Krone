package com.sofato.krone.groups.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.crypto.Fingerprint
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEnrollment: () -> Unit,
    viewModel: GroupsSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showDisableConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GroupsSettingsViewModel.Event.Error -> snackbar.showSnackbar(event.message)
                GroupsSettingsViewModel.Event.Disabled -> snackbar.showSnackbar("Groups disabled and purged.")
            }
        }
    }

    if (showDisableConfirm) {
        AlertDialog(
            onDismissRequest = { showDisableConfirm = false },
            title = { Text("Disable Groups?") },
            text = {
                Text(
                    "This deletes your device identity, all local group data, and " +
                        "notifies the server you're leaving. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDisableConfirm = false
                    viewModel.disable()
                }) {
                    Text("Disable", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableConfirm = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Groups", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Opt in to peer-to-peer shared expenses. Content is end-to-end encrypted; " +
                                "the server only relays opaque envelopes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.enabled,
                        enabled = !state.busy,
                        onCheckedChange = { checked ->
                            if (checked) viewModel.enable()
                            else showDisableConfirm = true
                        },
                    )
                }
            }

            if (state.enabled) {
                if (state.enrollment == null) {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                        ) {
                            Text("No server chosen yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Pick the donated server, or enter a custom one. You'll verify the " +
                                    "server's fingerprint before Krone sends anything to it.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Spacer(Modifier.height(Dimens.SpacingSm))
                            Button(
                                onClick = onNavigateToEnrollment,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Choose server") }
                        }
                    }
                } else {
                    EnrollmentSummaryCard(
                        url = state.enrollment!!.url,
                        serverFingerprint = state.enrollment!!.fingerprint,
                    )
                }

                state.identity?.let { identity ->
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpacingMd),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                        ) {
                            Text("Your device fingerprint", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Share these 8 words with others when they need to verify " +
                                    "you're the real Krone on this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(Dimens.SpacingXs))
                            FingerprintBlock(identity.fingerprint)
                            Text(
                                "Device id: ${identity.deviceIdHex}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showDisableConfirm = true },
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Disable and purge", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun EnrollmentSummaryCard(url: String, serverFingerprint: Fingerprint) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            Text("Server", style = MaterialTheme.typography.titleMedium)
            Text(url, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Dimens.SpacingXs))
            Text("Server fingerprint", style = MaterialTheme.typography.labelMedium)
            FingerprintBlock(serverFingerprint)
        }
    }
}

@Composable
internal fun FingerprintBlock(fingerprint: Fingerprint) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
        Text(
            fingerprint.words.joinToString(" "),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            fingerprint.shortHex,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
