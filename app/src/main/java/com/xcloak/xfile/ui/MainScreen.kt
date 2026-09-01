package com.xcloak.xfile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.xcloak.xfile.ui.components.XFileBackground
import com.xcloak.xfile.ui.navigation.Screen
import com.xcloak.xfile.ui.navigation.XFileNavGraph
import android.app.Activity

@Composable
fun MainScreen(
    onUpgradeClick: (Activity) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hierarchy?.any { 
        it.hasRoute<Screen.Home>() || it.hasRoute<Screen.Files>() || it.hasRoute<Screen.Settings>()
    } == true

    XFileBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    GlassBottomNav(
                        currentDestination = currentDestination,
                        onNavigate = { screen ->
                            navController.navigate(screen) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                XFileNavGraph(
                    navController = navController,
                    onUpgradeClick = onUpgradeClick
                )
            }
        }
    }
}

@Composable
fun GlassBottomNav(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(32.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .widthIn(max = 280.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Home>() } == true,
                    onClick = { onNavigate(Screen.Home) }
                )
                BottomNavItem(
                    icon = Icons.Default.InsertDriveFile,
                    label = "Files",
                    isSelected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Files>() } == true,
                    onClick = { onNavigate(Screen.Files) }
                )
                BottomNavItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = currentDestination?.hierarchy?.any { it.hasRoute<Screen.Settings>() } == true,
                    onClick = { onNavigate(Screen.Settings) }
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.weight(1f)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
