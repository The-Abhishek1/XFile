package com.xcloak.xfile.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.xcloak.xfile.ui.screens.splash.SplashScreen
import com.xcloak.xfile.ui.screens.onboarding.OnboardingScreen
import com.xcloak.xfile.ui.screens.home.HomeScreen
import com.xcloak.xfile.ui.screens.files.FilesScreen
import com.xcloak.xfile.ui.screens.settings.SettingsScreen
import com.xcloak.xfile.ui.screens.settings.ProUpgradeScreen
import android.app.Activity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.ui.screens.files.FilesViewModel

import androidx.navigation.toRoute
import com.xcloak.xfile.ui.screens.shared.SharedToolFlowScreen
import com.xcloak.xfile.ui.screens.pdf.PdfToolsScreen
import com.xcloak.xfile.ui.screens.images.ImageToolsScreen

import com.xcloak.xfile.core.billing.BillingManager
import com.xcloak.xfile.ui.theme.ThemeViewModel

@Composable
fun XFileNavGraph(
    navController: NavHostController,
    billingManager: BillingManager,
    onUpgradeClick: (Activity) -> Unit
) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val onboardingCompleted by themeViewModel.onboardingCompleted.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(300)) }
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onSplashComplete = {
                    if (onboardingCompleted) {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Splash) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Onboarding) {
                            popUpTo(Screen.Splash) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable<Screen.Onboarding> {
            OnboardingScreen(
                onGetStarted = {
                    themeViewModel.setOnboardingCompleted(true)
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home> {
            val filesViewModel: FilesViewModel = hiltViewModel()
            val recentFiles by filesViewModel.processedFiles.collectAsState()
            HomeScreen(
                recentFiles = recentFiles,
                onNavigateToPdf = { navController.navigate(Screen.PdfTools) },
                onNavigateToImages = { navController.navigate(Screen.ImageTools) },
                onNavigateToTool = { toolId -> navController.navigate(Screen.SharedToolFlow(toolId)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                onUpgradeClick = onUpgradeClick,
                billingManager = billingManager
            )
        }

        composable<Screen.PdfTools> {
            PdfToolsScreen(
                onBack = { navController.popBackStack() },
                onToolSelected = { toolId ->
                    navController.navigate(Screen.SharedToolFlow(toolId))
                }
            )
        }

        composable<Screen.ImageTools> {
            ImageToolsScreen(
                onBack = { navController.popBackStack() },
                onToolSelected = { toolId ->
                    navController.navigate(Screen.SharedToolFlow(toolId))
                }
            )
        }

        composable<Screen.SharedToolFlow> { backStackEntry ->
            val route: Screen.SharedToolFlow = backStackEntry.toRoute()
            SharedToolFlowScreen(
                toolId = route.toolId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Files> {
            FilesScreen()
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateToPro = { navController.navigate(Screen.ProUpgrade) },
                billingManager = billingManager
            )
        }

        composable<Screen.ProUpgrade> {
            ProUpgradeScreen(
                onBack = { navController.popBackStack() },
                onUpgradeClick = onUpgradeClick,
                billingManager = billingManager
            )
        }
    }
}