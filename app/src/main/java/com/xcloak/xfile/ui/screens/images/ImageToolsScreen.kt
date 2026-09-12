package com.xcloak.xfile.ui.screens.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.xfile.ui.components.AdvancedGlassCard
import com.xcloak.xfile.ui.screens.shared.ToolInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToolsScreen(
    onBack: () -> Unit,
    onToolSelected: (String) -> Unit
) {
    // FIX: removed Crop, Thumbnails, and Bulk Process — none of the three had any
    // engine method behind them, so all three errored out on tap. Compress,
    // Resize, Convert, and Privacy now handle multiple selected files natively
    // (see ToolFlowViewModel), which covers what "Bulk Process" was meant to do.
    // Crop and Thumbnails need dedicated UI (a crop canvas / thumbnail grid) that
    // isn't built yet — good candidates for a follow-up pass.
    val tools = listOf(
        ToolInfo("IMG_COMPRESS", "Compress", "Reduce size", Icons.Default.Compress),
        ToolInfo("IMG_RESIZE", "Resize", "Adjust dimensions", Icons.Default.AspectRatio),
        ToolInfo("IMG_CONVERT", "Convert", "Change format", Icons.Default.Transform),
        ToolInfo("IMG_ROTATE", "Rotate", "Turn 90° steps", Icons.AutoMirrored.Filled.RotateRight),
        ToolInfo("IMG_FLIP", "Flip", "Mirror image", Icons.Default.Flip),
        ToolInfo("IMG_METADATA", "Privacy", "Remove metadata", Icons.Default.Shield)
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Image Tools", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                Text(
                    text = "${tools.size} tools to edit, optimize, and protect images — all on-device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    ) {
padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tools) { tool ->
                ImageToolCard(tool) { onToolSelected(tool.id) }
            }
        }
    }
}

@Composable
fun ImageToolCard(tool: ToolInfo, onClick: () -> Unit) {
    AdvancedGlassCard(
        modifier = Modifier
            .height(160.dp)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}