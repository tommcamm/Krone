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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sofato.krone.R
import com.sofato.krone.ui.components.LocalHaptics
import com.sofato.krone.ui.components.rememberHaptics
import com.sofato.krone.ui.expenses.ExpenseBottomSheet
import com.sofato.krone.ui.expenses.ExpenseSheetViewModel
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
    val isHapticFeedbackEnabled by appViewModel.isHapticFeedbackEnabled.collectAsState()
    val haptics = rememberHaptics(enabled = isHapticFeedbackEnabled)

    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (darkModeOverride) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }

    KroneTheme(darkTheme = darkTheme, dynamicColor = isDynamicColorEnabled) {
        CompositionLocalProvider(LocalHaptics provides haptics) {
            if (isLoading) {
                Box(Modifier.fillMaxSize())
                return@CompositionLocalProvider
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
    val showFab = showBottomBar && isSavingsTab

    // Expense bottom sheet state
    var showExpenseSheet by remember { mutableStateOf(false) }
    val expenseSheetViewModel: ExpenseSheetViewModel = hiltViewModel()

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
                    onClick = { navController.navigate(KroneDestination.AddSavingsBucket) },
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_savings_bucket),
                    )
                }
            }
        },
    ) { innerPadding ->
        KroneNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onAddExpense = { categoryId ->
                expenseSheetViewModel.resetForNew(categoryId)
                showExpenseSheet = true
            },
            onEditExpense = { expenseId ->
                expenseSheetViewModel.loadForEdit(expenseId)
                showExpenseSheet = true
            },
        )
    }

    if (showExpenseSheet) {
        ExpenseBottomSheet(
            viewModel = expenseSheetViewModel,
            onDismiss = { showExpenseSheet = false },
        )
    }
}
