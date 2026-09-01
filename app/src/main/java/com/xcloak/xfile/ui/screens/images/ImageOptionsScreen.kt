package com.xcloak.xfile.ui.screens.images

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.xfile.ui.components.GlassButton
import com.xcloak.xfile.ui.components.GlassCard
import com.xcloak.xfile.ui.components.ToolLayout

@Composable
fun ImageOptionsScreen(
    toolId: String,
    quality: Int,
    format: Bitmap.CompressFormat,
    width: Int,
    height: Int,
    keepAspectRatio: Boolean,
    metadataSummary: Map<String, String>,
    onQualityChange: (Int) -> Unit,
    onFormatChange: (Bitmap.CompressFormat) -> Unit,
    onDimensionsChange: (Int, Int) -> Unit,
    onAspectRatioToggle: (Boolean) -> Unit,
    onProcess: () -> Unit,
    onBack: () -> Unit
) {
    ToolLayout(
        title = toolId.replace("IMG_", "").lowercase().replaceFirstChar { it.uppercase() },
        subtitle = "Configure your image processing preferences.",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Options",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (toolId == "IMG_COMPRESS") {
                        Text("Quality: $quality%", color = Color.White)
                        Slider(
                            value = quality.toFloat(),
                            onValueChange = { onQualityChange(it.toInt()) },
                            valueRange = 1f..100f
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        FormatPicker(format, onFormatChange)
                    }
                    
                    if (toolId == "IMG_RESIZE") {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = if (width == 0) "" else width.toString(),
                                onValueChange = { onDimensionsChange(it.toIntOrNull() ?: 0, height) },
                                label = { Text("Width") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = if (height == 0) "" else height.toString(),
                                onValueChange = { onDimensionsChange(width, it.toIntOrNull() ?: 0) },
                                label = { Text("Height") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = keepAspectRatio, onCheckedChange = onAspectRatioToggle)
                            Text("Keep Aspect Ratio", color = Color.White)
                        }
                    }

                    if (toolId == "IMG_METADATA") {
                        Text("Detected Metadata:", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        metadataSummary.forEach { (key, value) ->
                            Text("$key: $value", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            GlassButton(
                text = if (toolId == "IMG_METADATA") "Strip & Save" else "Apply & Process",
                onClick = onProcess,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FormatPicker(selected: Bitmap.CompressFormat, onSelect: (Bitmap.CompressFormat) -> Unit) {
    val formats = listOf(Bitmap.CompressFormat.JPEG, Bitmap.CompressFormat.PNG, Bitmap.CompressFormat.WEBP)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        formats.forEach { format ->
            val isSelected = selected == format
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(format) },
                label = { Text(format.name) }
            )
        }
    }
}
