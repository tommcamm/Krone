package com.sofato.krone.ui.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.R
import com.sofato.krone.ui.theme.Dimens
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CurrencySettingsViewModel = hiltViewModel(),
) {
    val currencies by viewModel.currencies.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val homeCurrencyCode by viewModel.homeCurrencyCode.collectAsState()
    val togglingCode by viewModel.togglingCode.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val rateFetchFailedMsg = stringResource(R.string.currency_enable_failed)
    val refreshFailedMsg = stringResource(R.string.rates_refresh_failed)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CurrencySettingsViewModel.CurrencySettingsEvent.RateFetchFailed -> {
                    snackbarHostState.showSnackbar(
                        message = "$rateFetchFailedMsg ${event.currencyCode}",
                    )
                }
                CurrencySettingsViewModel.CurrencySettingsEvent.RefreshFailed -> {
                    snackbarHostState.showSnackbar(message = refreshFailedMsg)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.currency_settings)) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                    }
                },
                actions = {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = viewModel::onRefreshRates) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_rates))
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Last updated info
            lastSyncTime?.let { time ->
                Text(
                    text = stringResource(R.string.rates_updated, formatTimeAgo(time)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingXs),
                )
            } ?: run {
                Text(
                    text = stringResource(R.string.rates_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingXs),
                )
            }

            Spacer(Modifier.height(Dimens.SpacingSm))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(currencies, key = { it.currency.code }) { item ->
                    CurrencyRow(
                        item = item,
                        homeCurrencyCode = homeCurrencyCode,
                        isToggling = togglingCode == item.currency.code,
                        onToggle = { enabled ->
                            viewModel.onToggleCurrency(item.currency.code, enabled)
                        },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Dimens.SpacingMd))
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(
    item: CurrencyWithRate,
    homeCurrencyCode: String,
    isToggling: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingMd, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
            ) {
                Text(
                    text = "${item.currency.symbol} ${item.currency.code}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (item.isHome) {
                    Text(
                        text = stringResource(R.string.home_currency),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = item.currency.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            item.rateToHome?.let { rate ->
                Text(
                    text = "1 ${item.currency.code} = %.4f $homeCurrencyCode".format(rate.rate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (isToggling) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Switch(
                checked = item.currency.isEnabled,
                onCheckedChange = onToggle,
                enabled = !item.isHome,
            )
        }
    }
}

private fun formatTimeAgo(instant: Instant): String {
    val duration = Clock.System.now() - instant
    return when {
        duration < 1.minutes -> "just now"
        duration < 1.hours -> "${duration.inWholeMinutes}m ago"
        duration < 1.days -> "${duration.inWholeHours}h ago"
        else -> "${duration.inWholeDays}d ago"
    }
}
