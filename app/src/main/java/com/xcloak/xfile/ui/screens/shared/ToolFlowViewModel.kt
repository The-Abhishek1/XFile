package com.xcloak.xfile.ui.screens.shared

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.xfile.core.database.FileDao
import com.xcloak.xfile.core.database.ProcessedFile
import com.xcloak.xfile.core.image.ImageProcessor
import com.xcloak.xfile.core.pdf.PdfProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface FlowState {
    data object Idle : FlowState
    data class Options(
        val selectedUris: List<Uri>,
        val pageRange: String = "",
        val rotationDegrees: Int = 90,
        val quality: Int = 80,
        val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        val width: Int = 0,
        val height: Int = 0,
        val keepAspectRatio: Boolean = true,
        val metadataSummary: Map<String, String> = emptyMap()
    ) : FlowState
    data class Processing(val progress: Float, val status: String) : FlowState
    data class Result(
        val originalSize: Long,
        val resultSize: Long,
        val outputUri: Uri,
        val multipleOutputs: List<Uri> = emptyList()
    ) : FlowState
    data class Error(val message: String) : FlowState
}

@HiltViewModel
class ToolFlowViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfProcessor: PdfProcessor,
    private val imageProcessor: ImageProcessor,
    private val fileDao: FileDao
) : ViewModel() {

    private val _flowState = MutableStateFlow<FlowState>(FlowState.Idle)
    val flowState: StateFlow<FlowState> = _flowState.asStateFlow()

    fun setFiles(uris: List<Uri>) {
        val metadata = if (uris.isNotEmpty() && uris[0].toString().contains("image")) {
            imageProcessor.getMetadataSummary(uris[0])
        } else emptyMap()
        
        _flowState.value = FlowState.Options(uris, metadataSummary = metadata)
    }

    fun updateOptions(
        pageRange: String? = null,
        rotationDegrees: Int? = null,
        quality: Int? = null,
        format: Bitmap.CompressFormat? = null,
        width: Int? = null,
        height: Int? = null,
        keepAspectRatio: Boolean? = null
    ) {
        val current = _flowState.value
        if (current is FlowState.Options) {
            _flowState.value = current.copy(
                pageRange = pageRange ?: current.pageRange,
                rotationDegrees = rotationDegrees ?: current.rotationDegrees,
                quality = quality ?: current.quality,
                format = format ?: current.format,
                width = width ?: current.width,
                height = height ?: current.height,
                keepAspectRatio = keepAspectRatio ?: current.keepAspectRatio
            )
        }
    }

    fun startProcessing(toolId: String) {
        val currentOptions = _flowState.value as? FlowState.Options ?: return
        val uris = currentOptions.selectedUris
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _flowState.value = FlowState.Processing(0.1f, "Processing...")
            
            val result: kotlin.Result<*> = when (toolId) {
                "PDF_MERGE" -> pdfProcessor.mergePdfs(uris, "merged_${System.currentTimeMillis()}.pdf")
                "PDF_SPLIT" -> pdfProcessor.splitPdf(uris[0])
                "PDF_EXTRACT" -> pdfProcessor.extractPages(uris[0], currentOptions.pageRange, "extracted_${System.currentTimeMillis()}.pdf")
                "PDF_DELETE" -> pdfProcessor.deletePages(uris[0], currentOptions.pageRange, "deleted_${System.currentTimeMillis()}.pdf")
                "PDF_ROTATE" -> pdfProcessor.rotatePages(uris[0], currentOptions.pageRange, currentOptions.rotationDegrees, "rotated_${System.currentTimeMillis()}.pdf")
                "PDF_WATERMARK" -> pdfProcessor.addWatermark(uris[0], currentOptions.pageRange, "watermarked_${System.currentTimeMillis()}.pdf")
                
                "IMG_COMPRESS" -> imageProcessor.compressImage(uris[0], currentOptions.quality, currentOptions.format, "compressed_${System.currentTimeMillis()}.jpg")
                "IMG_RESIZE" -> imageProcessor.resizeImage(uris[0], currentOptions.width, currentOptions.height, "resized_${System.currentTimeMillis()}.jpg", currentOptions.keepAspectRatio)
                "IMG_CONVERT" -> imageProcessor.convertImage(uris[0], currentOptions.format, "converted_${System.currentTimeMillis()}.jpg")
                "IMG_METADATA" -> imageProcessor.stripMetadata(uris[0], "stripped_${System.currentTimeMillis()}.jpg")
                
                else -> kotlin.Result.failure<File>(Exception("Unknown tool: $toolId"))
            }

            _flowState.value = FlowState.Processing(0.9f, "Finalizing...")

            result.onSuccess { output ->
                viewModelScope.launch {
                    val savedFile = if (output is File) {
                        ProcessedFile(
                            name = output.name,
                            timestamp = System.currentTimeMillis(),
                            size = output.length(),
                            type = if (toolId.startsWith("PDF")) "PDF" else "IMAGE",
                            tool = toolId.replace("PDF_", "").replace("IMG_", ""),
                            uri = Uri.fromFile(output).toString()
                        )
                    } else null
                    
                    savedFile?.let { fileDao.insertFile(it) }
                }

                if (output is File) {
                    _flowState.value = FlowState.Result(
                        originalSize = getUrisSize(uris),
                        resultSize = output.length(),
                        outputUri = Uri.fromFile(output)
                    )
                } else if (output is List<*>) {
                    val files = output.filterIsInstance<File>()
                    _flowState.value = FlowState.Result(
                        originalSize = getUrisSize(uris),
                        resultSize = files.sumOf { it.length() },
                        outputUri = Uri.fromFile(files.firstOrNull() ?: File("")),
                        multipleOutputs = files.map { Uri.fromFile(it) }
                    )
                }
            }.onFailure { e ->
                _flowState.value = FlowState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    private fun getUrisSize(uris: List<Uri>): Long {
        var totalSize = 0L
        uris.forEach { uri ->
            try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                    totalSize += it.length
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        return totalSize
    }

    fun reset() {
        _flowState.value = FlowState.Idle
    }
}
