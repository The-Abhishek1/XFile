package com.xcloak.xfile.ui.screens.pdf.split

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.ui.components.ToolLayout

@Composable
fun PdfSplitScreen(
    onBack: () -> Unit,
    viewModel: PdfSplitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectFile(it) }
    }

    ToolLayout(
        title = "Split PDF",
        subtitle = "Separate pages into individual documents",
        onBack = onBack
    ) {
        if (uiState.selectedUri == null) {
            Button(
                onClick = { launcher.launch("application/pdf") },
                modifier = Modifier.padding(top = 32.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Select PDF")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Selected: ${uiState.selectedUri?.path?.split("/")?.lastOrNull()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.pageRange,
                onValueChange = { viewModel.updateRange(it) },
                label = { Text("Page Range (e.g. 1-3)") },
                placeholder = { Text("Leave empty for single pages") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.splitPdf() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSplitting,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isSplitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Split PDF")
                }
            }
        }

        if (uiState.splitFiles.isNotEmpty()) {
            Text(
                text = "Successfully split into ${uiState.splitFiles.size} files!",
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
