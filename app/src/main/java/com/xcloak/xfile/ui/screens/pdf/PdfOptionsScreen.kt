package com.xcloak.xfile.ui.screens.pdf

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun PdfOptionsScreen(
    toolId: String,
    pageRange: String,
    rotationDegrees: Int,
    quality: Int,
    watermarkText: String,
    onPageRangeChange: (String) -> Unit,
    onRotationChange: (Int) -> Unit,
    onQualityChange: (Int) -> Unit,
    onWatermarkTextChange: (String) -> Unit,
    onProcess: () -> Unit,
    onBack: () -> Unit
) {
    ToolLayout(
        title = toolId.replace("PDF_", "").lowercase().replaceFirstChar { it.uppercase() },
        subtitle = "Configure your preferences before processing.",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Tool Options",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (toolId in listOf("PDF_EXTRACT", "PDF_DELETE", "PDF_ROTATE")) {
                        OutlinedTextField(
                            value = pageRange,
                            onValueChange = onPageRangeChange,
                            label = { Text("Page Range (e.g. 1-3, 5)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // FIX: PDF_WATERMARK previously showed only the page-range field
                    // above and had no way to enter the actual watermark text.
                    if (toolId == "PDF_WATERMARK") {
                        OutlinedTextField(
                            value = watermarkText,
                            onValueChange = onWatermarkTextChange,
                            label = { Text("Watermark text") },
                            placeholder = { Text("e.g. CONFIDENTIAL") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (toolId == "PDF_ROTATE") {
                        Text(
                            text = "Rotation Angle: $rotationDegrees°",
                            color = Color.White,
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

                    // NEW: PDF_COMPRESS had a tile but no options UI or engine hookup at all.
                    if (toolId == "PDF_COMPRESS") {
                        Text(
                            text = "Quality: $quality%",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Lower quality = smaller file size",
                            color = Color.White.copy(alpha = 0.5f),
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
                        Text(
                            text = "Page numbers will be added to the bottom center of every page, starting at 1.",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (toolId == "PDF_CONVERT") {
                        Text(
                            text = "Each selected image becomes one page, in the order you picked them.",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            GlassButton(
                text = "Apply & Process",
                onClick = onProcess,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}