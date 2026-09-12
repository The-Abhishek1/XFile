package com.xcloak.xfile.ui.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast

import com.xcloak.xfile.core.files.FileActions
import com.xcloak.xfile.ui.components.GlassButton
import com.xcloak.xfile.ui.screens.pdf.PdfOptionsScreen
import com.xcloak.xfile.ui.screens.images.ImageOptionsScreen
import com.xcloak.xfile.core.notification.NotificationHelper

import android.app.Activity
import com.xcloak.xfile.core.ads.AdManager

@Composable
fun SharedToolFlowScreen(
    toolId: String,
    onBack: () -> Unit,
    viewModel: ToolFlowViewModel = hiltViewModel()
) {
    val state by viewModel.flowState.collectAsState()
    val isPro by viewModel.billingManager.isPro.collectAsState()
    val context = LocalContext.current

    // Load interstitial when entering options
    LaunchedEffect(state) {
        if (state is FlowState.Options && !isPro) {
            viewModel.adManager.loadInterstitial(context as Activity)
        }
    }

    when (val s = state) {
        is FlowState.Idle -> {
            FilePickerScreen(
                title = toolId.replace("_", " "),
                // FIX: every "PDF_"-prefixed tool was forced to pick application/pdf,
                // which made "Image → PDF" impossible to use — it needs images in.
                allowedFormats = if (toolId == "PDF_CONVERT") {
                    listOf("image/*")
                } else if (toolId.startsWith("PDF")) {
                    listOf("application/pdf")
                } else {
                    listOf("image/*")
                },
                onFilesSelected = { viewModel.setFiles(it) },
                onBack = onBack
            )
        }
        is FlowState.Options -> {
            if (toolId.startsWith("PDF")) {
                PdfOptionsScreen(
                    toolId = toolId,
                    selectedUris = s.selectedUris,
                    selectedFileNames = s.selectedFileNames,
                    pageRange = s.pageRange,
                    rotationDegrees = s.rotationDegrees,
                    quality = s.quality,
                    watermarkText = s.watermarkText,
                    watermarkOpacity = s.watermarkOpacity,
                    watermarkRotation = s.watermarkRotation,
                    watermarkSize = s.watermarkSize,
                    numbersStartAt = s.numbersStartAt,
                    numbersFontSize = s.numbersFontSize,
                    numbersPosition = s.numbersPosition,
                    convertPageSize = s.convertPageSize,
                    convertImageScale = s.convertImageScale,
                    outputFileName = s.outputFileName,
                    splitEveryN = s.splitEveryN,
                    outputPrefix = s.outputPrefix,
                    thumbnails = s.thumbnails,
                    onPageRangeChange = { viewModel.updateOptions(pageRange = it) },
                    onRotationChange = { viewModel.updateOptions(rotationDegrees = it) },
                    onQualityChange = { viewModel.updateOptions(quality = it) },
                    onWatermarkTextChange = { viewModel.updateOptions(watermarkText = it) },
                    onWatermarkOpacityChange = { viewModel.updateOptions(watermarkOpacity = it) },
                    onWatermarkRotationChange = { viewModel.updateOptions(watermarkRotation = it) },
                    onWatermarkSizeChange = { viewModel.updateOptions(watermarkSize = it) },
                    onNumbersStartAtChange = { viewModel.updateOptions(numbersStartAt = it) },
                    onNumbersFontSizeChange = { viewModel.updateOptions(numbersFontSize = it) },
                    onNumbersPositionChange = { viewModel.updateOptions(numbersPosition = it) },
                    onConvertPageSizeChange = { viewModel.updateOptions(convertPageSize = it) },
                    onConvertImageScaleChange = { viewModel.updateOptions(convertImageScale = it) },
                    onOutputFileNameChange = { viewModel.updateOptions(outputFileName = it) },
                    onSplitEveryNChange = { viewModel.updateOptions(splitEveryN = it) },
                    onOutputPrefixChange = { viewModel.updateOptions(outputPrefix = it) },
                    onAddFiles = { viewModel.setFiles(s.selectedUris + it) },
                    onRemoveFile = { viewModel.removeFile(it) },
                    onReorder = { from, to -> viewModel.reorderFiles(from, to) },
                    onProcess = { viewModel.startProcessing(toolId) },
                    onBack = { viewModel.reset() }
                )
            } else if (toolId.startsWith("IMG")) {
                ImageOptionsScreen(
                    toolId = toolId,
                    quality = s.quality,
                    format = s.format,
                    width = s.width,
                    height = s.height,
                    keepAspectRatio = s.keepAspectRatio,
                    metadataSummary = s.metadataSummary,
                    outputFileName = s.outputFileName,
                    rotationDegrees = s.rotationDegrees,
                    flipHorizontal = s.flipHorizontal,
                    flipVertical = s.flipVertical,
                    thumbnails = s.thumbnails.mapValues { it.value.firstOrNull() },
                    onQualityChange = { viewModel.updateOptions(quality = it) },
                    onFormatChange = { viewModel.updateOptions(format = it) },
                    onDimensionsChange = { w, h -> viewModel.updateOptions(width = w, height = h) },
                    onAspectRatioToggle = { viewModel.updateOptions(keepAspectRatio = it) },
                    onOutputFileNameChange = { viewModel.updateOptions(outputFileName = it) },
                    onRotationChange = { viewModel.updateOptions(rotationDegrees = it) },
                    onFlipChange = { h, v -> viewModel.updateOptions(flipHorizontal = h, flipVertical = v) },
                    onProcess = { viewModel.startProcessing(toolId) },
                    onBack = { viewModel.reset() }
                )
            } else {
                // Neither a PDF_ nor IMG_ tool (not currently reachable — every tile in
                // PdfToolsScreen/ImageToolsScreen uses one of those prefixes — but calling
                // startProcessing directly here, during composition, would previously have
                // re-launched it on every recomposition instead of once.
                LaunchedEffect(toolId) { viewModel.startProcessing(toolId) }
            }
        }
        is FlowState.Processing -> {
            ProcessingScreen(
                progress = s.progress,
                status = s.status
            )
        }
        is FlowState.Result -> {
            var adShown by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                if (!isPro && !adShown) {
                    viewModel.adManager.showInterstitial(context as Activity) {
                        adShown = true
                    }
                }
            }

            ResultScreen(
                originalSize = s.originalSize,
                resultSize = s.resultSize,
                fileCount = s.outputFiles.size,
                // FIX: these three were no-ops (`/* Logic */`) — tapping Share, Save, or
                // Open on a finished file did nothing at all.
                onShare = { FileActions.share(context, s.outputFiles) },
                onSave = {
                    val saved = FileActions.save(context, s.outputFiles)
                    if (saved) {
                        viewModel.showDownloadNotification(s.primaryFile.name)
                        Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Couldn't save file", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpen = { FileActions.open(context, s.primaryFile) },
                onProcessAnother = { viewModel.reset() }
            )
        }
        is FlowState.Error -> {
            // FIX: this branch rendered nothing — a failed job (bad page range,
            // corrupt PDF, unreadable image, etc.) just left a blank screen with no
            // way back other than the system back button.
            ErrorScreen(
                message = s.message,
                onRetry = { viewModel.reset() },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.displayMedium,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        GlassButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        androidx.compose.material3.TextButton(onClick = onBack) {
            Text("Back to tools", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }
    }
}