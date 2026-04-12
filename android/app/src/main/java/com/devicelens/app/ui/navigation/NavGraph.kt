package com.devicelens.app.ui.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devicelens.app.ui.debug.DebugLogScreen
import com.devicelens.app.ui.details.DeviceDetailsScreen
import com.devicelens.app.ui.locate.LocateModeSheet
import com.devicelens.app.ui.locate.LocateViewModel
import com.devicelens.app.ui.onboarding.OnboardingScreen
import com.devicelens.app.ui.settings.SettingsSheet
import com.devicelens.app.ui.setup.SetupScreen
import com.devicelens.app.ui.status.StatusScreen
import com.devicelens.app.ui.status.StatusViewModel

sealed class Screen(val route: String) {
    data object Status : Screen("status")
    data object Setup : Screen("setup")
    data object DebugLog : Screen("debug_log")
    data object Onboarding : Screen("onboarding")
    data object DeviceDetails : Screen("device_details/{deviceId}") {
        fun createRoute(deviceId: Long) = "device_details/$deviceId"
    }
}

@Composable
fun DeviceLensNavHost(
    isLoggedIn: Boolean,
    onLoginSuccess: () -> Unit
) {
    val navController = rememberNavController()
    var showSettings by remember { mutableStateOf(false) }
    var locateDeviceId by remember { mutableStateOf<Long?>(null) }

    // Settings sheet
    if (showSettings) {
        val statusVM: StatusViewModel = hiltViewModel()
        SettingsSheet(
            onDismiss = { showSettings = false },
            onReset = {
                statusVM.resetAll()
                showSettings = false
            }
        )
    }

    // Locate mode sheet
    locateDeviceId?.let { deviceId ->
        LocateModeSheet(
            onDismiss = { locateDeviceId = null }
        )
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Status.route else Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    onLoginSuccess()
                    navController.navigate(Screen.Status.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Status.route) {
            StatusScreen(
                onDeviceClick = { deviceId ->
                    navController.navigate(Screen.DeviceDetails.createRoute(deviceId))
                },
                onNavigateToSetup = {
                    navController.navigate(Screen.Setup.route)
                },
                onOpenSettings = { showSettings = true },
                onOpenDebugLog = {
                    navController.navigate(Screen.DebugLog.route)
                }
            )
        }
// ... [rest of the composables] ...

        composable(Screen.Setup.route) {
            val statusVM: StatusViewModel = hiltViewModel(
                navController.getBackStackEntry(Screen.Status.route)
            )
            SetupScreen(
                onComplete = {
                    statusVM.onSetupCompleted()
                    navController.popBackStack(Screen.Status.route, inclusive = false)
                }
            )
        }

        composable(Screen.DebugLog.route) {
            DebugLogScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DeviceDetails.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.LongType })
        ) {
            DeviceDetailsScreen(
                onBack = { navController.popBackStack() },
                onLocate = { deviceId -> locateDeviceId = deviceId }
            )
        }
    }
}
