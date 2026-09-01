package com.xcloak.xfile.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Splash : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Files : Screen

    @Serializable
    data class SharedToolFlow(val toolId: String) : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object ProUpgrade : Screen

    @Serializable
    data object PdfTools : Screen

    @Serializable
    data object ImageTools : Screen

    // Tool flows will be added in Phase 4/5
    @Serializable
    data object PdfMerge : Screen
    
    @Serializable
    data object PdfSplit : Screen
    
    @Serializable
    data object ImageResize : Screen
    
    @Serializable
    data object ImageConvert : Screen
    
    @Serializable
    data object ImageMetadata : Screen
    
    @Serializable
    data object PdfWatermark : Screen
}
