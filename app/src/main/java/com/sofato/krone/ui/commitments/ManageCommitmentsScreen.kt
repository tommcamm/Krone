package com.sofato.krone.ui.commitments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sofato.krone.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCommitmentsScreen(
    onNavigateBack: () -> Unit,
    onManageRecurring: () -> Unit,
    onManageSavings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage commitments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            Text(
                text = "Edit monthly/yearly fixed expenses and savings contributions in one place.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            CommitmentCard(
                title = "Recurring expenses",
                description = "Edit monthly and yearly fixed expenses.",
                onClick = onManageRecurring,
            )

            CommitmentCard(
                title = "Savings contributions",
                description = "Edit monthly savings buckets and contribution amounts.",
                onClick = onManageSavings,
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingXxl))
        }
    }
}

@Composable
private fun CommitmentCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingMd),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
