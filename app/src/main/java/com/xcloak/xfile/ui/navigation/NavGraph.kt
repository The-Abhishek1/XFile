package com.xcloak.xfile.ui.navigation

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

import androidx.navigation.toRoute
import com.xcloak.xfile.ui.screens.shared.SharedToolFlowScreen
import com.xcloak.xfile.ui.screens.pdf.PdfToolsScreen
import com.xcloak.xfile.ui.screens.images.ImageToolsScreen

@Composable
fun XFileNavGraph(
    navController: NavHostController,
    onUpgradeClick: (Activity) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }
        
        composable<Screen.Onboarding> {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onNavigateToPdf = { navController.navigate(Screen.PdfTools) },
                onNavigateToImages = { navController.navigate(Screen.ImageTools) },
                onNavigateToSettings = { navController.navigate(Screen.Settings) },
                onUpgradeClick = onUpgradeClick
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
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.ProUpgrade> {
            ProUpgradeScreen(
                onBack = { navController.popBackStack() },
                onUpgradeClick = onUpgradeClick
            )
        }
    }
}
