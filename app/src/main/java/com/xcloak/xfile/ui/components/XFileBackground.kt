package com.xcloak.xfile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.xcloak.xfile.ui.theme.NavyDeep

@Composable
fun XFileBackground(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    0.0f to NavyDeep,
                    0.5f to Color(0xFF1E1B4B), // Deep Indigo
                    1.0f to Color(0xFF312E81)  // Lighter Deep Indigo
                )
            )
    ) {
        content()
    }
}
