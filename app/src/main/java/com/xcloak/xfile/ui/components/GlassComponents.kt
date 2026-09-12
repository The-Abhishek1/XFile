package com.xcloak.xfile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val isDark = isSystemInDarkTheme()
    
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.clip(shape),
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.1f else 0.05f),
        border = BorderStroke(1.dp, if (isDark) {
            Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent)
            )
        } else {
            androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        }),
        shape = shape
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0f),
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0f)
        ),
        start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f),
        end = androidx.compose.ui.geometry.Offset(shimmerOffset + 100f, 100f)
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .background(shimmerBrush, RoundedCornerShape(24.dp)),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isSystemInDarkTheme()) 0.15f else 0.08f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) {
            Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), Color.Transparent)
            )
        } else {
            androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
        }),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun GlassChip(
    text: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.15f else 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.3f else 0.6f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
