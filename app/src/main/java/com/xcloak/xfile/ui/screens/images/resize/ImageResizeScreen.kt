package com.xcloak.xfile.ui.screens.images.resize

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.ui.components.ToolLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageResizeScreen(
    onBack: () -> Unit,
    viewModel: ImageResizeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectImage(it) }
    }

    ToolLayout(
        title = "Resize Image",
        subtitle = "Reduce image size without quality loss.",
        onBack = onBack
    ) {
        if (uiState.selectedUri == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Select Image")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Selected: ${uiState.selectedUri?.path?.substringAfterLast('/')}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.width,
                        onValueChange = { viewModel.updateDimensions(it, uiState.height) },
                        label = { Text("Width (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    OutlinedTextField(
                        value = uiState.height,
                        onValueChange = { viewModel.updateDimensions(uiState.width, it) },
                        label = { Text("Height (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.resizeImage() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isResizing && uiState.width.isNotEmpty() && uiState.height.isNotEmpty(),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (uiState.isResizing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Resize & Download", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        uiState.resizedFileUri?.let { _ ->
            AlertDialog(
                onDismissRequest = { /* Handle */ },
                confirmButton = {
                    Button(onClick = { /* Share */ }) {
                        Text("Download")
                    }
                },
                title = { Text("Done!") },
                text = { Text("Your image has been resized locally.") }
            )
        }
    }
}
