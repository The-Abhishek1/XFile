package com.xcloak.xfile.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

import com.xcloak.xfile.ui.screens.pdf.PdfOptionsScreen
import com.xcloak.xfile.ui.screens.images.ImageOptionsScreen

@Composable
fun SharedToolFlowScreen(
    toolId: String,
    onBack: () -> Unit,
    viewModel: ToolFlowViewModel = hiltViewModel()
) {
    val state by viewModel.flowState.collectAsState()

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
                    pageRange = s.pageRange,
                    rotationDegrees = s.rotationDegrees,
                    quality = s.quality,
                    watermarkText = s.watermarkText,
                    onPageRangeChange = { viewModel.updateOptions(pageRange = it) },
                    onRotationChange = { viewModel.updateOptions(rotationDegrees = it) },
                    onQualityChange = { viewModel.updateOptions(quality = it) },
                    onWatermarkTextChange = { viewModel.updateOptions(watermarkText = it) },
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
                    onQualityChange = { viewModel.updateOptions(quality = it) },
                    onFormatChange = { viewModel.updateOptions(format = it) },
                    onDimensionsChange = { w, h -> viewModel.updateOptions(width = w, height = h) },
                    onAspectRatioToggle = { viewModel.updateOptions(keepAspectRatio = it) },
                    onProcess = { viewModel.startProcessing(toolId) },
                    onBack = { viewModel.reset() }
                )
            } else {
                viewModel.startProcessing(toolId)
            }
        }
        is FlowState.Processing -> {
            ProcessingScreen(
                progress = s.progress,
                status = s.status
            )
        }
        is FlowState.Result -> {
            ResultScreen(
                originalSize = s.originalSize,
                resultSize = s.resultSize,
                onShare = { /* Logic */ },
                onSave = { /* Logic */ },
                onOpen = { /* Logic */ },
                onProcessAnother = { viewModel.reset() }
            )
        }
        is FlowState.Error -> {
            // Error handling UI
        }
    }
}