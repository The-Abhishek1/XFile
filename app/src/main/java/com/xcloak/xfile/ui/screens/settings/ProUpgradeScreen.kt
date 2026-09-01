package com.xcloak.xfile.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.xfile.ui.components.GlassButton
import com.xcloak.xfile.ui.components.GlassCard
import com.xcloak.xfile.ui.theme.ElectricBlue
import com.xcloak.xfile.ui.theme.VioletSoft

@Composable
fun ProUpgradeScreen(
    onBack: () -> Unit,
    onUpgradeClick: (Activity) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = ElectricBlue,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Go Pro",
            style = MaterialTheme.typography.displayLarge,
            color = Color.White
        )

        Text(
            text = "Unlock the full potential of XFile",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(2.dp, Brush.linearGradient(listOf(ElectricBlue, VioletSoft))),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                FeatureRow("Bulk Image Processing", true)
                FeatureRow("PDF Watermarking", true)
                FeatureRow("OCR Text Extraction", true)
                FeatureRow("Zero Ads", true)
                FeatureRow("Priority Processing", true)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "One-time purchase. Lifetime access.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        GlassButton(
            text = "Unlock Pro — ₹299",
            onClick = { (context as? Activity)?.let { onUpgradeClick(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        
        TextButton(onClick = { /* Restore */ }) {
            Text("Restore Purchase", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FeatureRow(text: String, isPro: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPro) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isPro) ElectricBlue else Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
