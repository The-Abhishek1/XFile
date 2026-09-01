package com.xcloak.xfile.ui.screens.images.convert

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.ui.components.ToolLayout

@Composable
fun ImageConvertScreen(
    onBack: () -> Unit,
    viewModel: ImageConvertViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectImage(it) }
    }

    ToolLayout(
        title = "Convert Image",
        subtitle = "Change image formats instantly",
        onBack = onBack
    ) {
        if (uiState.selectedUri == null) {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.padding(top = 32.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Select Image")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Selected: ${uiState.selectedUri?.path?.split("/")?.lastOrNull()}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Target Format", style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FormatChip("JPEG", Bitmap.CompressFormat.JPEG, uiState.targetFormat) { viewModel.setFormat(it) }
                FormatChip("PNG", Bitmap.CompressFormat.PNG, uiState.targetFormat) { viewModel.setFormat(it) }
                FormatChip("WEBP", Bitmap.CompressFormat.WEBP, uiState.targetFormat) { viewModel.setFormat(it) }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { viewModel.convertImage() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isConverting,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isConverting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Convert & Save")
                }
            }
        }
        
        uiState.convertedFileUri?.let {
            Text(
                text = "Conversion Successful!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        uiState.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatChip(
    label: String,
    format: Bitmap.CompressFormat,
    selectedFormat: Bitmap.CompressFormat,
    onSelect: (Bitmap.CompressFormat) -> Unit
) {
    FilterChip(
        selected = format == selectedFormat,
        onClick = { onSelect(format) },
        label = { Text(label) }
    )
}
