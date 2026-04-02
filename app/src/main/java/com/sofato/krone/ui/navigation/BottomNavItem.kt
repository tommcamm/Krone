package com.sofato.krone.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.sofato.krone.R

enum class BottomNavItem(
    val destination: KroneDestination,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DASHBOARD(KroneDestination.Dashboard, R.string.nav_dashboard, Icons.Filled.Today, Icons.Outlined.Today),
    BUDGET(KroneDestination.Budget, R.string.nav_budget, Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    SAVINGS(KroneDestination.Savings, R.string.nav_savings, Icons.Filled.Savings, Icons.Outlined.Savings),
    INSIGHTS(KroneDestination.Insights, R.string.nav_insights, Icons.Filled.Insights, Icons.Outlined.Insights),
    SETTINGS(KroneDestination.Settings, R.string.settings, Icons.Filled.Settings, Icons.Outlined.Settings),
}
