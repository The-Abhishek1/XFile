package com.xcloak.xfile.ui.screens.images

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    outputFileName: String,
    rotationDegrees: Int,
    flipHorizontal: Boolean,
    flipVertical: Boolean,
    thumbnails: Map<Uri, Bitmap?>,
    onQualityChange: (Int) -> Unit,
    onFormatChange: (Bitmap.CompressFormat) -> Unit,
    onDimensionsChange: (Int, Int) -> Unit,
    onAspectRatioToggle: (Boolean) -> Unit,
    onOutputFileNameChange: (String) -> Unit,
    onRotationChange: (Int) -> Unit,
    onFlipChange: (Boolean, Boolean) -> Unit,
    onProcess: () -> Unit,
    onBack: () -> Unit
) {
    ToolLayout(
        title = toolId.replace("_", " "),
        subtitle = "Configure your image processing preferences.",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Preview Box (Styled like Picker)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                val strokeColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                                )
                            )
                        },
                        color = strokeColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                        )
                    )
                }
                
                // In Image tools, ToolFlowViewModel.setFiles always sets uris[0] as the reference
                val firstUri = thumbnails.keys.firstOrNull()
                val thumb = thumbnails[firstUri]
                if (thumb != null) {
                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                    Surface(
                        modifier = Modifier.fillMaxHeight(0.85f).aspectRatio(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.2f else 0.6f))
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = thumb.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Options",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (toolId == "IMG_COMPRESS") {
                        Text(
                            text = "Compression Level",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Extreme" to 30,
                                "Balanced" to 65,
                                "High" to 90
                            ).forEach { (label, value) ->
                                val isSelected = quality == value
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onQualityChange(value) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = com.xcloak.xfile.ui.theme.ElectricBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = com.xcloak.xfile.ui.theme.ElectricBlue,
                                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Manual Quality: $quality%",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = quality.toFloat(),
                            onValueChange = { onQualityChange(it.toInt()) },
                            valueRange = 1f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Output Format", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FormatPicker(format, onFormatChange)
                    }

                    if (toolId == "IMG_CONVERT") {
                        Text("Target Format", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        FormatPicker(format, onFormatChange)
                    }
                    
                    if (toolId == "IMG_RESIZE") {
                        Text("Presets", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Small" to 800,
                                "Social" to 1080,
                                "HD" to 1920,
                                "4K" to 3840
                            ).forEach { (label, size) ->
                                val isSelected = width == size || height == size
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onDimensionsChange(size, size) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = com.xcloak.xfile.ui.theme.ElectricBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = com.xcloak.xfile.ui.theme.ElectricBlue,
                                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = if (width == 0) "" else width.toString(),
                                onValueChange = { onDimensionsChange(it.toIntOrNull() ?: 0, height) },
                                label = { Text("Width (px)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                            OutlinedTextField(
                                value = if (height == 0) "" else height.toString(),
                                onValueChange = { onDimensionsChange(width, it.toIntOrNull() ?: 0) },
                                label = { Text("Height (px)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = keepAspectRatio,
                                onCheckedChange = onAspectRatioToggle,
                                colors = CheckboxDefaults.colors(checkmarkColor = MaterialTheme.colorScheme.onPrimary)
                            )
                            Text("Keep Aspect Ratio", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Output Format", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FormatPicker(format, onFormatChange)
                    }

                    if (toolId == "IMG_METADATA") {
                        Text("Detected Metadata", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (metadataSummary.isEmpty()) {
                            Text("No EXIF metadata found in this image.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                        } else {
                            metadataSummary.forEach { (key, value) ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("$key: ", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
                                    Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    if (toolId == "IMG_ROTATE") {
                        Text(
                            text = "Rotation Angle: $rotationDegrees°",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = rotationDegrees.toFloat(),
                            onValueChange = { onRotationChange(it.toInt()) },
                            valueRange = 0f..270f,
                            steps = 2,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Output Format", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FormatPicker(format, onFormatChange)
                    }

                    if (toolId == "IMG_FLIP") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Flip Horizontally", color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = flipHorizontal,
                                onCheckedChange = { onFlipChange(it, flipVertical) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Flip Vertically", color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = flipVertical,
                                onCheckedChange = { onFlipChange(flipHorizontal, it) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Output Format", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FormatPicker(format, onFormatChange)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = outputFileName,
                        onValueChange = onOutputFileNameChange,
                        label = { Text("Output Filename") },
                        placeholder = { Text("e.g. MyImage") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = "Format extension will be added automatically",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        GlassButton(
            text = when (toolId) {
                "IMG_COMPRESS" -> "Compress Images"
                "IMG_RESIZE" -> "Resize Images"
                "IMG_CONVERT" -> "Convert Images"
                "IMG_METADATA" -> "Strip & Save"
                "IMG_ROTATE" -> "Rotate Images"
                "IMG_FLIP" -> "Flip Images"
                else -> "Apply & Process"
            },
            onClick = onProcess,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
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
