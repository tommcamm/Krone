package com.sofato.krone.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sofato.krone.R
import com.sofato.krone.ui.onboarding.OnboardingScreen
import com.sofato.krone.ui.theme.KroneTheme

@Composable
fun KroneApp(
    appViewModel: KroneAppViewModel = hiltViewModel(),
) {
    val isLoading by appViewModel.isLoading.collectAsState()
    val hasCompletedOnboarding by appViewModel.hasCompletedOnboarding.collectAsState()
    val darkModeOverride by appViewModel.darkModeOverride.collectAsState()
    val isDynamicColorEnabled by appViewModel.isDynamicColorEnabled.collectAsState()

    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (darkModeOverride) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }

    KroneTheme(darkTheme = darkTheme, dynamicColor = isDynamicColorEnabled) {
        if (isLoading) {
            Box(Modifier.fillMaxSize())
            return@KroneTheme
        }

        if (!hasCompletedOnboarding) {
            OnboardingScreen(
                onComplete = { appViewModel.onOnboardingComplete() },
            )
        } else {
            MainApp()
        }
    }
}

@Composable
private fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.let { dest ->
        BottomNavItem.entries.any { dest.hasRoute(it.destination::class) }
    } ?: true
    val isSavingsTab = currentDestination?.hasRoute(KroneDestination.Savings::class) == true
    val isDashboardTab = currentDestination?.hasRoute(KroneDestination.Dashboard::class) == true
    val isSettingsTab = currentDestination?.hasRoute(KroneDestination.Settings::class) == true
    val showFab = showBottomBar && !isSettingsTab && !isDashboardTab

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                KroneBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        if (isSavingsTab) {
                            navController.navigate(KroneDestination.AddSavingsBucket)
                        } else {
                            navController.navigate(KroneDestination.AddExpense())
                        }
                    },
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = if (isSavingsTab) {
                            stringResource(R.string.add_savings_bucket)
                        } else {
                            stringResource(R.string.add_expense)
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        KroneNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
