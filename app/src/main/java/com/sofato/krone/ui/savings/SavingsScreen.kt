package com.sofato.krone.ui.savings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sofato.krone.ui.savings.components.SavingsBucketCard
import com.sofato.krone.ui.theme.Dimens
import com.sofato.krone.util.CurrencyFormatter

@Composable
fun SavingsScreen(
    onAddBucket: () -> Unit,
    onBucketClick: (Long) -> Unit,
    viewModel: SavingsViewModel = hiltViewModel(),
) {
    val buckets by viewModel.buckets.collectAsState()
    val totalMonthly by viewModel.totalMonthlyContribution.collectAsState()
    val currency by viewModel.homeCurrency.collectAsState()

    val curr = currency ?: return

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBucket) {
                Icon(Icons.Default.Add, contentDescription = "Add savings bucket")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            item { Spacer(Modifier.height(Dimens.SpacingMd)) }

            // Summary card
            item(key = "summary") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevation),
                ) {
                    Text(
                        text = "Total monthly contributions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = Dimens.SpacingMd, top = Dimens.SpacingMd, end = Dimens.SpacingMd),
                    )
                    Text(
                        text = CurrencyFormatter.formatDisplay(totalMonthly, curr),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(start = Dimens.SpacingMd, bottom = Dimens.SpacingMd, end = Dimens.SpacingMd),
                    )
                }
            }

            // Bucket list
            items(items = buckets, key = { it.id }) { bucket ->
                SavingsBucketCard(
                    bucket = bucket,
                    currency = curr,
                    onClick = { onBucketClick(bucket.id) },
                )
            }

            item { Spacer(Modifier.height(Dimens.SpacingXxl)) }
        }
    }
}
