package com.xcloak.xfile.ui.screens.images.metadata

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.ui.components.GlassyCard
import com.xcloak.xfile.ui.components.ToolLayout

@Composable
fun ImageMetadataScreen(
    onBack: () -> Unit,
    viewModel: ImageMetadataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectImage(it) }
    }

    ToolLayout(
        title = "Remove Metadata",
        subtitle = "Strip private EXIF data from your photos",
        onBack = onBack
    ) {
        if (uiState.selectedUri == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Select Image to Inspect")
                }
            }
        } else {
            Text(
                text = "Metadata Found",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.metadataSummary.toList()) { (key, value) ->
                    GlassyCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                if (uiState.metadataSummary.isEmpty()) {
                    item {
                        Text(
                            "No metadata detected in this image.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.stripMetadata() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isProcessing && uiState.metadataSummary.isNotEmpty(),
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Remove Metadata & Save")
                }
            }

            if (uiState.successFileUri != null) {
                Text(
                    "Success! Cleaned image saved.",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (uiState.error != null) {
                Text(
                    "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Select Another Image")
            }
        }
    }
}
