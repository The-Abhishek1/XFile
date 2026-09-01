package com.xcloak.xfile.ui.screens.pdf

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
fun PdfToolsScreen(
    onBack: () -> Unit,
    onToolSelected: (String) -> Unit
) {
    // FIX: "Reorder" was removed — it had no options UI (needs a drag-and-drop
    // page grid) and no view-model wiring, so tapping it errored. "Delete Pages"
    // was added — the engine method already existed and was already wired in the
    // view model, it just had no tile to launch it.
    val tools = listOf(
        ToolInfo("PDF_MERGE", "Merge", "Combine files", Icons.Default.Merge),
        ToolInfo("PDF_SPLIT", "Split", "Extract pages", Icons.Default.CallSplit),
        ToolInfo("PDF_EXTRACT", "Extract", "Specific pages", Icons.Default.ContentCopy),
        ToolInfo("PDF_DELETE", "Delete Pages", "Remove pages", Icons.Default.DeleteOutline),
        ToolInfo("PDF_ROTATE", "Rotate", "Rotate pages", Icons.Default.RotateRight),
        ToolInfo("PDF_WATERMARK", "Watermark", "Add protection", Icons.Default.Water),
        ToolInfo("PDF_COMPRESS", "Compress", "Reduce size", Icons.Default.Compress),
        ToolInfo("PDF_NUMBERS", "Numbers", "Add pagination", Icons.Default.Pin),
        ToolInfo("PDF_CONVERT", "Image → PDF", "Create PDF", Icons.Default.PictureAsPdf)
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("PDF Tools", fontWeight = FontWeight.Bold, color = Color.White) },
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
                PdfToolCard(tool) { onToolSelected(tool.id) }
            }
        }
    }
}

@Composable
fun PdfToolCard(tool: ToolInfo, onClick: () -> Unit) {
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