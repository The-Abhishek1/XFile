package com.xcloak.xfile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = Color(0xFF38BDF8),
    tertiary = IndigoBrand,
    onPrimary = NavyDeep,
    background = NavyDeep,
    onBackground = TextWhite,
    surface = NavyDeep,
    onSurface = TextWhite,
    outline = GlassBorder,
    surfaceVariant = GlassWhite,
    error = ErrorRed
)

// FIX: light scheme was imported but never defined/used — "Switch Theme" had nothing to switch to.
private val LightColorScheme = lightColorScheme(
    primary = IndigoBrand,
    secondary = ElectricBlue,
    tertiary = VioletSoft,
    onPrimary = Color.White,
    background = Color(0xFFF1F5F9), // Slate 100
    onBackground = NavyDeep,
    surface = Color.White,
    onSurface = NavyDeep,
    surfaceVariant = Color(0xFFE2E8F0), // Slate 200
    onSurfaceVariant = Color(0xFF475569), // Slate 600
    outline = Color(0xFFCBD5E1), // Slate 300
    error = Color(0xFFB91C1C)
)

@Composable
fun XFileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // FIX: previously always used DarkColorScheme regardless of the darkTheme parameter.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}