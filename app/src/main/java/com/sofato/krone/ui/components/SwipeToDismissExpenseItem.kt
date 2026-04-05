package com.sofato.krone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.model.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissExpenseItem(
    expense: Expense,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    homeCurrency: Currency? = null,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.4f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
            }
            // Always return false so the box animates back to Settled.
            // The actual item removal happens via the list reacting to the delete.
            // This prevents a stale EndToStart state from re-triggering onDismiss
            // when an item is restored via undo.
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            ExpenseItem(expense = expense, onClick = onClick, homeCurrency = homeCurrency)
        }
    }
}
