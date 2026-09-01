package com.xcloak.xfile.ui.screens.images

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.xfile.ui.components.AdvancedGlassCard
import com.xcloak.xfile.ui.screens.shared.ToolInfo
import com.xcloak.xfile.ui.theme.ElectricBlue

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
        ToolInfo("IMG_METADATA", "Privacy", "Remove metadata", Icons.Default.Shield)
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Image Tools", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
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
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}