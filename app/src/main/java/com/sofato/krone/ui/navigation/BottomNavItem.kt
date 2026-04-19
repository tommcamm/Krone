package com.sofato.krone.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.ui.graphics.vector.ImageVector
import com.sofato.krone.R

enum class BottomNavItem(
    val destination: KroneDestination,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DASHBOARD(KroneDestination.Dashboard, R.string.nav_dashboard, Icons.Filled.Home, Icons.Outlined.Home),
    BUDGET(KroneDestination.Budget, R.string.nav_budget, Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    SAVINGS(KroneDestination.Savings, R.string.nav_savings, Icons.Filled.Savings, Icons.Outlined.Savings),
    INSIGHTS(KroneDestination.Insights, R.string.nav_insights, Icons.Filled.Insights, Icons.Outlined.Insights),
}
