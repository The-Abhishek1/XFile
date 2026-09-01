package com.xcloak.xfile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.core.preferences.AppTheme
import com.xcloak.xfile.ui.components.AdvancedGlassCard
import com.xcloak.xfile.ui.components.GlassCard
import com.xcloak.xfile.ui.theme.ElectricBlue
import com.xcloak.xfile.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPro: () -> Unit,
    onBack: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Pro Banner
        AdvancedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToPro
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = ElectricBlue)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Go Pro", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 20.sp)
                    Text("Remove ads & unlock all tools", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("General", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(12.dp))

        SettingsRow(
            Icons.Default.Palette, 
            "Appearance", 
            "Switch Theme",
            onClick = {
                // Simplified toggle logic for now
                viewModel.setTheme(AppTheme.DARK) 
            }
        )

        SettingsRow(Icons.Default.Folder, "Default Save Location", "/Documents/XFile") {}
        SettingsRow(Icons.Default.DeleteSweep, "Clear Cache", "12.4 MB") {}

        Spacer(modifier = Modifier.height(32.dp))

        Text("Support", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(12.dp))

        SettingsRow(Icons.Default.Info, "About XFile", "Version 1.0.0") {}
        SettingsRow(Icons.Default.PrivacyTip, "Privacy Policy", "") {}
    }
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.2f))
        }
    }
}
