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
    onPageRangeChange: (String) -> Unit,
    onRotationChange: (Int) -> Unit,
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
                    
                    if (toolId in listOf("PDF_EXTRACT", "PDF_DELETE", "PDF_ROTATE", "PDF_WATERMARK")) {
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
