package com.xcloak.xfile.ui.screens.pdf.merge

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.ui.components.ToolLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfMergeScreen(
    onBack: () -> Unit,
    viewModel: PdfMergeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.addFiles(uris)
    }

    ToolLayout(
        title = "Merge PDFs",
        subtitle = "Combine multiple PDFs into one document.",
        onBack = onBack
    ) {
        if (uiState.selectedUris.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { launcher.launch("application/pdf") },
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select PDF Files")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.selectedUris) { uri ->
                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.2f else 0.6f))
                    ) {
                        ListItem(
                            headlineContent = { Text(uri.path?.substringAfterLast('/') ?: "Unknown file") },
                            trailingContent = {
                                IconButton(onClick = { viewModel.removeFile(uri) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
                
                item {
                    TextButton(
                        onClick = { launcher.launch("application/pdf") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add more files")
                    }
                }
            }
            
            Button(
                onClick = { viewModel.mergeFiles() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                enabled = !uiState.isMerging && uiState.selectedUris.size >= 2,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (uiState.isMerging) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Merge PDFs", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        uiState.mergedFileUri?.let { uri ->
            AlertDialog(
                onDismissRequest = { /* Handle */ },
                confirmButton = {
                    Button(onClick = { /* Share */ }) {
                        Text("Download / Share")
                    }
                },
                title = { Text("Done!") },
                text = { Text("Your files have been merged locally.") }
            )
        }
    }
}
