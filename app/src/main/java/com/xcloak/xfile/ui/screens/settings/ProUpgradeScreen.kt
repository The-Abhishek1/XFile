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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.xcloak.xfile.core.billing.BillingManager
import com.xcloak.xfile.ui.components.GlassButton
import com.xcloak.xfile.ui.components.GlassCard

@Composable
fun ProUpgradeScreen(
    onBack: () -> Unit,
    onUpgradeClick: (Activity) -> Unit,
    billingManager: BillingManager
) {
    val context = LocalContext.current
    val proPrice by billingManager.proPrice.collectAsState()

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
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Go Pro",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Unlock the full potential of XFile",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(2.dp, Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary, 
                            MaterialTheme.colorScheme.tertiary
                        )
                    )),
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
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        GlassButton(
            text = proPrice,
            onClick = { (context as? Activity)?.let { onUpgradeClick(it) } },
            modifier = Modifier.fillMaxWidth()
        )
        
        TextButton(onClick = { /* Restore */ }) {
            Text("Restore Purchase", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 12.sp)
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
            tint = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
