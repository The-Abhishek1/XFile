package com.xcloak.xfile.ui.screens.home

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.ui.components.ProBadge
import com.xcloak.xfile.core.billing.BillingManager
import com.xcloak.xfile.core.database.ProcessedFile
import com.xcloak.xfile.core.files.FileActions
import com.xcloak.xfile.ui.components.PrivacyBadge
import com.xcloak.xfile.ui.components.GlassCard
import com.xcloak.xfile.ui.theme.ElectricBlue
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.xcloak.xfile.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ToolCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tools: List<ToolItem>
)

data class ToolItem(
    val id: String,
    val name: String,
    val description: String
)

@Composable
fun HomeScreen(
    recentFiles: List<ProcessedFile>,
    onNavigateToPdf: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToTool: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onUpgradeClick: (Activity) -> Unit,
    billingManager: BillingManager
) {
    val isPro by billingManager.isPro.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Hero Section
        Box(contentAlignment = Alignment.Center) {
            val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
            val glowScale by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_scale"
            )
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_alpha"
            )

            // Outer Glow
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(glowScale)
                    .alpha(glowAlpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Surface(
                modifier = Modifier.size(92.dp),
                color = Color.Transparent,
                shape = RoundedCornerShape(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    val logoRes = if (isDark) R.drawable.ic_brand_logo_dark else R.drawable.ic_brand_logo
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "XFile Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            if (isPro) {
                ProBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-10).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your files. Your device.\nZero uploads.",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-1).sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Powerful PDF and image tools that run entirely on your device. Process files without sending them to a server.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrivacyBadge()

        if (recentFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(48.dp))
            QuickAccessSection(
                files = recentFiles.take(5),
                onFileClick = { file ->
                    val f = File(android.net.Uri.parse(file.uri).path ?: "")
                    if (f.exists()) {
                        FileActions.open(context, f)
                    } else {
                        onNavigateToSettings() // Fallback if file missing
                    }
                }
            )
        }
        
        if (!isPro) {
            Spacer(modifier = Modifier.height(48.dp))
            ProBanner(
                onClick = { (context as? Activity)?.let { onUpgradeClick(it) } }
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        // PDF Tools Section
        ToolSection(
            title = "PDF Tools",
            description = "Edit, organize, convert and optimize PDFs locally.",
            tools = listOf(
                ToolItem("PDF_MERGE", "Merge", "Combine files"),
                ToolItem("PDF_SPLIT", "Split", "Extract pages"),
                ToolItem("PDF_WATERMARK", "Watermark", "Add protection"),
                ToolItem("PDF_COMPRESS", "Compress", "Reduce size"),
                ToolItem("PDF_NUMBERS", "Numbers", "Add pagination"),
                ToolItem("PDF_EXTRACT", "Extract", "Pick pages")
            ),
            onToolClick = onNavigateToTool,
            onSeeAll = onNavigateToPdf
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Image Tools Section
        ToolSection(
            title = "Image Tools",
            description = "Compress, resize, convert and optimize images.",
            tools = listOf(
                ToolItem("IMG_RESIZE", "Resize", "Adjust pixels"),
                ToolItem("IMG_CONVERT", "Convert", "Change formats"),
                ToolItem("IMG_ROTATE", "Rotate", "Turn 90° steps"),
                ToolItem("IMG_FLIP", "Flip", "Mirror image"),
                ToolItem("IMG_METADATA", "Privacy", "Strip EXIF"),
                ToolItem("IMG_COMPRESS", "Compress", "Reduce size")
            ),
            onToolClick = onNavigateToTool,
            onSeeAll = onNavigateToImages
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Privacy Architecture Section
        PrivacyArchitectureSection()

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun ToolSection(
    title: String,
    description: String,
    tools: List<ToolItem>,
    onToolClick: (String) -> Unit,
    onSeeAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            TextButton(
                onClick = onSeeAll,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    "See all",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid of tools
        Column {
            tools.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pair.forEach { tool ->
                        ToolCard(tool, modifier = Modifier.weight(1f)) { onToolClick(tool.id) }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ToolCard(
    tool: ToolItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tint = when {
        tool.id.contains("PDF") -> MaterialTheme.colorScheme.primary
        tool.id.contains("IMG") -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    GlassCard(
        modifier = modifier.height(140.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        tint.copy(alpha = 0.12f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        tool.id.contains("MERGE") -> Icons.Default.Merge
                        tool.id.contains("SPLIT") -> Icons.AutoMirrored.Filled.CallSplit
                        tool.id.contains("WATERMARK") -> Icons.Default.Water
                        tool.id.contains("COMPRESS") -> Icons.Default.Compress
                        tool.id.contains("RESIZE") -> Icons.Default.AspectRatio
                        tool.id.contains("NUMBERS") -> Icons.Default.Pin
                        tool.id.contains("CONVERT") -> Icons.Default.Transform
                        tool.id.contains("METADATA") -> Icons.Default.Shield
                        tool.id.contains("ROTATE") -> Icons.AutoMirrored.Filled.RotateRight
                        tool.id.contains("FLIP") -> Icons.Default.Flip
                        else -> Icons.Default.Extension
                    },
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun QuickAccessSection(
    files: List<ProcessedFile>,
    onFileClick: (ProcessedFile) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Access",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            files.forEach { file ->
                QuickFileCard(file) { onFileClick(file) }
            }
        }
    }
}

@Composable
fun QuickFileCard(
    file: ProcessedFile,
    onClick: () -> Unit
) {
    val tint = if (file.type == "PDF") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    
    GlassCard(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (file.type == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Image,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${file.tool} • ${formatDate(file.timestamp)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ProBanner(
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.1f else 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isDark) {
            Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent)
            )
        } else {
            androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        })
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Go Pro",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Unlock all tools & remove ads",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun PrivacyArchitectureSection() {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Private by Architecture",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "XFile is designed so your files never leave your device. We use local processing to ensure your data stays private.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
