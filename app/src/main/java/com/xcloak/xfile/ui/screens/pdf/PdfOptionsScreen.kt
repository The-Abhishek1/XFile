package com.xcloak.xfile.ui.screens.pdf

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.xfile.ui.components.GlassButton
import com.xcloak.xfile.ui.components.GlassCard
import com.xcloak.xfile.ui.components.ToolLayout
import com.xcloak.xfile.ui.theme.ElectricBlue

@Composable
fun FileOrderCard(
    name: String,
    thumbnail: Bitmap?,
    onRemove: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DragHandle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                if (thumbnail != null) {
                    androidx.compose.foundation.Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1)
            
            if (onMoveUp != null) {
                IconButton(onClick = onMoveUp) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            if (onMoveDown != null) {
                IconButton(onClick = onMoveDown) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun PdfOptionsScreen(
    toolId: String,
    selectedUris: List<Uri>,
    selectedFileNames: List<String>,
    pageRange: String,
    rotationDegrees: Int,
    quality: Int,
    watermarkText: String,
    watermarkOpacity: Float,
    watermarkRotation: Int,
    watermarkSize: Float,
    numbersStartAt: Int,
    numbersFontSize: Float,
    numbersPosition: String,
    convertPageSize: String,
    convertImageScale: String,
    outputFileName: String,
    splitEveryN: Int,
    outputPrefix: String,
    thumbnails: Map<Uri, List<Bitmap>>,
    onPageRangeChange: (String) -> Unit,
    onRotationChange: (Int) -> Unit,
    onQualityChange: (Int) -> Unit,
    onWatermarkTextChange: (String) -> Unit,
    onWatermarkOpacityChange: (Float) -> Unit,
    onWatermarkRotationChange: (Int) -> Unit,
    onWatermarkSizeChange: (Float) -> Unit,
    onNumbersStartAtChange: (Int) -> Unit,
    onNumbersFontSizeChange: (Float) -> Unit,
    onNumbersPositionChange: (String) -> Unit,
    onConvertPageSizeChange: (String) -> Unit,
    onConvertImageScaleChange: (String) -> Unit,
    onOutputFileNameChange: (String) -> Unit,
    onSplitEveryNChange: (Int) -> Unit,
    onOutputPrefixChange: (String) -> Unit,
    onAddFiles: (List<Uri>) -> Unit,
    onRemoveFile: (Int) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onProcess: () -> Unit,
    onBack: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        onAddFiles(uris)
    }

    ToolLayout(
        title = toolId.replace("_", " "),
        subtitle = "Configure your preferences before processing.",
        onBack = onBack
    ) {
        if (toolId == "PDF_MERGE") {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Files to Merge",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(selectedUris) { index, uri ->
                        FileOrderCard(
                            name = selectedFileNames.getOrElse(index) { uri.path?.substringAfterLast('/') ?: "File ${index + 1}" },
                            thumbnail = thumbnails[uri]?.firstOrNull(),
                            onRemove = { onRemoveFile(index) },
                            onMoveUp = if (index > 0) { { onReorder(index, index - 1) } } else null,
                            onMoveDown = if (index < selectedUris.size - 1) { { onReorder(index, index + 1) } } else null
                        )
                    }
                    
                    item {
                        Column {
                            TextButton(
                                onClick = { launcher.launch(arrayOf("application/pdf")) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ElectricBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add more PDFs", color = ElectricBlue, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = outputFileName,
                                onValueChange = onOutputFileNameChange,
                                label = { Text("Output Filename") },
                                placeholder = { Text("e.g. MyMergedFile") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                )
                            )
                            Text(
                                text = ".pdf will be added automatically",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlassButton(
                    text = "Merge PDFs",
                    onClick = onProcess,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scrollable PDF Preview Box
                if (toolId in listOf("PDF_EXTRACT", "PDF_DELETE", "PDF_ROTATE", "PDF_WATERMARK", "PDF_COMPRESS", "PDF_NUMBERS")) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
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
                        
                        val pages = thumbnails[selectedUris.firstOrNull()] ?: emptyList()
                        if (pages.isNotEmpty()) {
                            // Vertical scrollable list of pages
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                itemsIndexed(pages) { _, bitmap ->
                                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(0.9f).aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat()),
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = if (isDark) 0.1f else 0.3f))
                                    ) {
                                        androidx.compose.foundation.Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        } else {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = ElectricBlue.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "Tool Options",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (toolId in listOf("PDF_EXTRACT", "PDF_DELETE", "PDF_ROTATE")) {
                            OutlinedTextField(
                                value = pageRange,
                                onValueChange = onPageRangeChange,
                                label = { Text("Page Range") },
                                placeholder = { Text("e.g. 1-3, 5") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                            Text(
                                text = "Use 1-3 for range, or 1, 3 for individual pages",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (toolId == "PDF_WATERMARK") {
                            OutlinedTextField(
                                value = watermarkText,
                                onValueChange = onWatermarkTextChange,
                                label = { Text("Watermark text") },
                                placeholder = { Text("e.g. CONFIDENTIAL") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Opacity: ${(watermarkOpacity * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface)
                            Slider(
                                value = watermarkOpacity,
                                onValueChange = onWatermarkOpacityChange,
                                valueRange = 0.1f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text("Rotation: $watermarkRotation°", color = MaterialTheme.colorScheme.onSurface)
                            Slider(
                                value = watermarkRotation.toFloat(),
                                onValueChange = { onWatermarkRotationChange(it.toInt()) },
                                valueRange = 0f..360f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text("Font Size: ${watermarkSize.toInt()}", color = MaterialTheme.colorScheme.onSurface)
                            Slider(
                                value = watermarkSize,
                                onValueChange = onWatermarkSizeChange,
                                valueRange = 12f..120f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (toolId == "PDF_SPLIT") {
                            Text(
                                text = "Split every ${if (splitEveryN == 1) "page" else "$splitEveryN pages"}",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = splitEveryN.toFloat(),
                                onValueChange = { onSplitEveryNChange(it.toInt()) },
                                valueRange = 1f..10f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = outputPrefix,
                                onValueChange = onOutputPrefixChange,
                                label = { Text("File Name Prefix") },
                                placeholder = { Text("e.g. split") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (toolId == "PDF_ROTATE") {
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
                        }

                        if (toolId == "PDF_COMPRESS") {
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
                                            selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                            selectedLabelColor = ElectricBlue,
                                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
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
                            Text(
                                text = "Lower quality = smaller file size",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            Slider(
                                value = quality.toFloat(),
                                onValueChange = { onQualityChange(it.toInt()) },
                                valueRange = 10f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (toolId == "PDF_NUMBERS") {
                            OutlinedTextField(
                                value = if (numbersStartAt == 0) "" else numbersStartAt.toString(),
                                onValueChange = { onNumbersStartAtChange(it.toIntOrNull() ?: 1) },
                                label = { Text("Start numbering at") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Position", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val positions = listOf("Top Left", "Top Center", "Top Right", "Bottom Left", "Bottom Center", "Bottom Right")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                positions.forEach { pos ->
                                    val isSelected = numbersPosition == pos
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onNumbersPositionChange(pos) },
                                        label = { Text(pos, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                            selectedLabelColor = ElectricBlue,
                                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Font Size: ${numbersFontSize.toInt()}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            Slider(
                                value = numbersFontSize,
                                onValueChange = onNumbersFontSizeChange,
                                valueRange = 8f..32f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (toolId == "PDF_CONVERT") {
                            Text("Page Size", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Original", "A4").forEach { size ->
                                    val isSelected = convertPageSize == size
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onConvertPageSizeChange(size) },
                                        label = { Text(size) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                            selectedLabelColor = ElectricBlue,
                                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                            
                            if (convertPageSize == "A4") {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Image Scaling", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Fit", "Fill", "Stretch").forEach { scale ->
                                        val isSelected = convertImageScale == scale
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onConvertImageScaleChange(scale) },
                                            label = { Text(scale) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                                selectedLabelColor = ElectricBlue,
                                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Image Quality: $quality%", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            Slider(
                                value = quality.toFloat(),
                                onValueChange = { onQualityChange(it.toInt()) },
                                valueRange = 10f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        // Shared Output Filename for non-Merge/non-Split tools
                        if (toolId != "PDF_SPLIT" && toolId != "PDF_MERGE") {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = outputFileName,
                                onValueChange = onOutputFileNameChange,
                                label = { Text("Output Filename") },
                                placeholder = { Text("e.g. MyProcessedFile") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                            Text(
                                text = ".pdf will be added automatically",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlassButton(
                    text = "Apply & Process",
                    onClick = onProcess,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
