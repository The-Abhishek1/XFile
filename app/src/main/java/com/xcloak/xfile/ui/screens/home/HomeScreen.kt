package com.xcloak.xfile.ui.screens.home

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.xfile.ui.components.PrivacyBadge

data class ToolCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tools: List<ToolItem>
)

data class ToolItem(
    val name: String,
    val description: String,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onNavigateToPdf: () -> Unit,
    onNavigateToImages: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onUpgradeClick: (Activity) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Hero Section
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "X",
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your files. Your device.\nZero uploads.",
            style = MaterialTheme.typography.displayMedium.copy(
                letterSpacing = (-1).sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Powerful PDF and image tools that run directly in your browser. Process files without sending them to a server.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrivacyBadge()

        Spacer(modifier = Modifier.height(64.dp))

        // Tool Sections
        ToolSection(
            title = "PDF Tools",
            description = "Edit, organize, convert and optimize PDFs locally.",
            tools = listOf(
                ToolItem("Merge PDF", "Combine files", onNavigateToPdf),
                ToolItem("Split PDF", "Extract pages", onNavigateToPdf),
                ToolItem("Watermark", "Add protection", onNavigateToPdf)
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        ToolSection(
            title = "Image Tools",
            description = "Compress, resize, convert and optimize images.",
            tools = listOf(
                ToolItem("Resize", "Perfect pixels", onNavigateToImages),
                ToolItem("Convert", "Change formats", onNavigateToImages),
                ToolItem("Privacy", "Remove metadata", onNavigateToImages)
            )
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Privacy Architecture Section
        PrivacyArchitectureSection()

        Spacer(modifier = Modifier.height(64.dp))

        TextButton(
            onClick = { (context as? Activity)?.let { onUpgradeClick(it) } },
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            Text("Unlock Pro Features", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ToolSection(
    title: String,
    description: String,
    tools: List<ToolItem>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.displayMedium, fontSize = 28.sp)
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            tools.forEach { tool ->
                ToolCard(tool)
            }
        }
    }
}

@Composable
fun ToolCard(tool: ToolItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { tool.onClick() },
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(tool.name, style = MaterialTheme.typography.headlineLarge)
                Text(
                    tool.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PrivacyArchitectureSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Private by Architecture",
                style = MaterialTheme.typography.displayMedium,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "XFile is designed so your files never leave your device. We use local processing to ensure your data stays private.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
