package com.xcloak.xfile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp)),
        color = surfaceColor.copy(alpha = if (isDark) 0.15f else 0.4f),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    outlineColor.copy(alpha = if (isDark) 0.2f else 0.8f),
                    outlineColor.copy(alpha = 0.05f)
                )
            )
        ),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            surfaceColor.copy(alpha = if (isDark) 0.15f else 0.2f),
                            surfaceColor.copy(alpha = if (isDark) 0.05f else 0.1f)
                        )
                    )
                )
                .padding(16.dp),
            content = content
        )
    }
}
