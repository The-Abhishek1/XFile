package com.xcloak.xfile.ui.screens.files

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.xcloak.xfile.core.database.ProcessedFile
import com.xcloak.xfile.core.files.FileActions
import com.xcloak.xfile.ui.theme.SuccessGreen
import com.xcloak.xfile.ui.components.GlassCard
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilesScreen(
    viewModel: FilesViewModel = hiltViewModel()
) {
    val files by viewModel.processedFiles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val context = LocalContext.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recent Files",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                // NEW: there was no sense of how much history you were looking at, or
                // how much space it's using — just a bare list.
                if (files.isNotEmpty()) {
                    Text(
                        text = "${files.size} file${if (files.size == 1) "" else "s"} • ${formatSize(files.sumOf { it.size })}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            if (files.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search files...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    }
                }
            } else null,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "PDF", "Images").forEach { type ->
                val isSelected = selectedType == type
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateSelectedType(type) },
                    label = { Text(type) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary 
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = if (isDark) 0.1f else 0.3f)
                        }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (files.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (searchQuery.isNotEmpty()) "No results found" else "Nothing processed yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Files you merge, compress, or convert will show up here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(files) { file ->
                    FileRow(
                        file = file,
                        onOpen = {
                            fileFromStoredUri(file.uri)?.let { FileActions.open(context, it) }
                        },
                        onShare = {
                            fileFromStoredUri(file.uri)?.let { FileActions.share(context, listOf(it)) }
                        },
                        onDelete = { viewModel.deleteFile(file) }
                    )
                }
            }
        }
    }
}

@Composable
fun FileRow(
    file: ProcessedFile,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val iconInfo = when (file.tool) {
        "MERGE" -> Icons.Default.Merge to MaterialTheme.colorScheme.primary
        "SPLIT" -> Icons.AutoMirrored.Filled.CallSplit to MaterialTheme.colorScheme.primary
        "EXTRACT" -> Icons.Default.ContentCopy to MaterialTheme.colorScheme.primary
        "DELETE" -> Icons.Default.DeleteOutline to MaterialTheme.colorScheme.error
        "ROTATE" -> Icons.AutoMirrored.Filled.RotateRight to MaterialTheme.colorScheme.primary
        "WATERMARK" -> Icons.Default.Water to MaterialTheme.colorScheme.primary
        "COMPRESS" -> Icons.Default.Compress to MaterialTheme.colorScheme.primary
        "NUMBERS" -> Icons.Default.Pin to MaterialTheme.colorScheme.primary
        "CONVERT" -> Icons.Default.Transform to MaterialTheme.colorScheme.primary
        "RESIZE" -> Icons.Default.AspectRatio to MaterialTheme.colorScheme.primary
        "METADATA" -> Icons.Default.Shield to SuccessGreen
        "FLIP" -> Icons.Default.Flip to MaterialTheme.colorScheme.primary
        else -> Icons.Default.InsertDriveFile to MaterialTheme.colorScheme.primary
    }
    val icon = iconInfo.first
    val tint = iconInfo.second

    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            Surface(
                modifier = Modifier.size(48.dp),
                color = tint.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = if (isDark) 0.2f else 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${file.tool} • ${formatDate(file.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(kb)
}

/** ProcessedFile.uri is stored as a file:// string (see ToolFlowViewModel) — resolve it
 *  back to a File so FileActions can hand it to FileProvider for a content:// Uri. */
private fun fileFromStoredUri(uriString: String): File? {
    val path = android.net.Uri.parse(uriString).path ?: return null
    val file = File(path)
    return file.takeIf { it.exists() }
}