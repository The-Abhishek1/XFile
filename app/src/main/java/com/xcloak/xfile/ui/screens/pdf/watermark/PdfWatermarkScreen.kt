package com.xcloak.xfile.ui.screens.pdf.watermark

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.ui.components.GlassyCard
import com.xcloak.xfile.ui.components.ToolLayout

@Composable
fun PdfWatermarkScreen(
    onBack: () -> Unit,
    viewModel: PdfWatermarkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.selectUri(uri)
    }

    ToolLayout(
        title = "Watermark PDF",
        subtitle = "Add security text overlays to your documents",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.selectedUri == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { launcher.launch("application/pdf") },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Select PDF File")
                    }
                }
            } else {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.selectedUri?.path?.split("/")?.lastOrNull() ?: "Selected PDF",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { launcher.launch("application/pdf") }) {
                                Text("Change")
                            }
                        }

                        OutlinedTextField(
                            value = uiState.watermarkText,
                            onValueChange = { viewModel.updateWatermarkText(it) },
                            label = { Text("Watermark Text") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Opacity: ${(uiState.opacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = uiState.opacity,
                            onValueChange = { viewModel.updateOpacity(it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Rotation: ${uiState.rotation}°",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = uiState.rotation.toFloat(),
                            onValueChange = { viewModel.updateRotation(it.toInt()) },
                            valueRange = 0f..360f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.applyWatermark() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.watermarkText.isNotBlank() && !uiState.isProcessing,
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Apply Watermark")
                    }
                }
            }

            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    uiState.resultUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { viewModel.resetResult() },
            confirmButton = {
                TextButton(onClick = { viewModel.resetResult() }) {
                    Text("OK")
                }
            },
            title = { Text("Success") },
            text = { Text("Watermark applied successfully! File saved to cache.") }
        )
    }
}
