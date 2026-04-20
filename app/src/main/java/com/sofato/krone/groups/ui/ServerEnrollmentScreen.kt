package com.sofato.krone.groups.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.crypto.Bip39
import com.sofato.krone.crypto.Fingerprint
import com.sofato.krone.crypto.FingerprintComputer
import com.sofato.krone.crypto.HexCodec
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerEnrollmentScreen(
    onNavigateBack: () -> Unit,
    onEnrolled: () -> Unit,
    viewModel: ServerEnrollmentViewModel = hiltViewModel(),
    bip39: Bip39 = remember { Bip39.loadEnglish() },
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is ServerEnrollmentViewModel.Event.EnrollmentCompleted) {
                onEnrolled()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose server") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
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
            if (state.busy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            state.errorMessage?.let { msg ->
                Text(msg, color = MaterialTheme.colorScheme.error)
            }

            DonatedServerCard(
                url = state.donatedUrl,
                pinnedFingerprint = remember(state.donatedPkHex) {
                    FingerprintComputer.fromPublicKey(HexCodec.decode(state.donatedPkHex), bip39)
                },
                busy = state.busy,
                onEnroll = viewModel::enrollDonated,
            )

            CustomServerCard(
                url = state.customUrl,
                allowHttp = state.allowHttp,
                pending = state.pending,
                busy = state.busy,
                onUrlChange = viewModel::updateCustomUrl,
                onFetch = viewModel::fetchCustomFingerprint,
                onConfirm = viewModel::confirmCustom,
            )
        }
    }
}

@Composable
private fun DonatedServerCard(
    url: String,
    pinnedFingerprint: Fingerprint,
    busy: Boolean,
    onEnroll: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        ) {
            Text("Donated server", style = MaterialTheme.typography.titleMedium)
            Text(
                "Krone operates a default server for users who don't want to self-host. " +
                    "Its fingerprint is pinned in this build — Krone will refuse to enroll " +
                    "if the real server returns a different one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(url, style = MaterialTheme.typography.bodyMedium)
            Text("Expected fingerprint", style = MaterialTheme.typography.labelMedium)
            FingerprintBlock(pinnedFingerprint)
            Spacer(Modifier.height(Dimens.SpacingSm))
            Button(
                onClick = onEnroll,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enroll with donated server") }
        }
    }
}

@Composable
private fun CustomServerCard(
    url: String,
    allowHttp: Boolean,
    pending: com.sofato.krone.groups.domain.model.PendingEnrollment?,
    busy: Boolean,
    onUrlChange: (String) -> Unit,
    onFetch: () -> Unit,
    onConfirm: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        ) {
            Text("Custom / self-hosted server", style = MaterialTheme.typography.titleMedium)
            Text(
                if (allowHttp) {
                    "Debug build: http:// is allowed for local testing. Use https:// in production."
                } else {
                    "Only https:// URLs are allowed on release builds."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                label = { Text("Server URL") },
                placeholder = { Text("https://groups.example.com") },
                singleLine = true,
                enabled = !busy && pending == null,
                modifier = Modifier.fillMaxWidth(),
            )
            if (pending == null) {
                Button(
                    onClick = onFetch,
                    enabled = !busy && url.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Fetch fingerprint") }
            } else {
                Text(
                    "Verify these 8 words match the server operator's records out-of-band " +
                        "(phone call, signed message, etc.) before tapping confirm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FingerprintBlock(pending.fingerprint)
                Text(
                    "Server ${pending.serverVersion} · protocol ${pending.protocolVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onConfirm,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Words match — confirm enrollment") }
            }
        }
    }
}
