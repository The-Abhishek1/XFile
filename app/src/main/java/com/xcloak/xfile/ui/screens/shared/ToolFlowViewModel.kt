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
        val metadataSummary: Map<String, String> = emptyMap(),
        // FIX: this field didn't exist — watermark text had nowhere to live, so
        // the view model fell back to reusing pageRange as the watermark text.
        val watermarkText: String = ""
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

    // Tools that make sense to run across every selected file, not just the first.
    // Merge and Convert-to-PDF already consume the whole list by design.
    private val bulkCapableImageTools = setOf("IMG_COMPRESS", "IMG_RESIZE", "IMG_CONVERT", "IMG_METADATA")

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
        keepAspectRatio: Boolean? = null,
        watermarkText: String? = null
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
                keepAspectRatio = keepAspectRatio ?: current.keepAspectRatio,
                watermarkText = watermarkText ?: current.watermarkText
            )
        }
    }

    fun startProcessing(toolId: String) {
        val currentOptions = _flowState.value as? FlowState.Options ?: return
        val uris = currentOptions.selectedUris
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _flowState.value = FlowState.Processing(0.1f, "Processing...")

            // Tools that take the whole file list at once (merge, image->pdf) or
            // only ever act on a single file (split, extract, etc.) go through the
            // single-result path below. Bulk-capable image tools loop separately.
            if (toolId in bulkCapableImageTools && uris.size > 1) {
                processImageBulk(toolId, uris, currentOptions)
                return@launch
            }

            val result: kotlin.Result<*> = when (toolId) {
                "PDF_MERGE" -> pdfProcessor.mergePdfs(uris, "merged_${System.currentTimeMillis()}.pdf")
                "PDF_SPLIT" -> pdfProcessor.splitPdf(uris[0])
                "PDF_EXTRACT" -> pdfProcessor.extractPages(uris[0], currentOptions.pageRange, "extracted_${System.currentTimeMillis()}.pdf")
                "PDF_DELETE" -> pdfProcessor.deletePages(uris[0], currentOptions.pageRange, "deleted_${System.currentTimeMillis()}.pdf")
                "PDF_ROTATE" -> pdfProcessor.rotatePages(uris[0], currentOptions.pageRange, currentOptions.rotationDegrees, "rotated_${System.currentTimeMillis()}.pdf")
                // FIX: was passing currentOptions.pageRange as the watermark text.
                "PDF_WATERMARK" -> pdfProcessor.addWatermark(uris[0], currentOptions.watermarkText, "watermarked_${System.currentTimeMillis()}.pdf")
                // NEW: these three tiles previously had no case here at all and fell
                // through to "Unknown tool".
                "PDF_COMPRESS" -> pdfProcessor.compressPdf(uris[0], currentOptions.quality, "compressed_${System.currentTimeMillis()}.pdf")
                "PDF_NUMBERS" -> pdfProcessor.addPageNumbers(uris[0], "numbered_${System.currentTimeMillis()}.pdf")
                "PDF_CONVERT" -> pdfProcessor.convertImagesToPdf(uris, "converted_${System.currentTimeMillis()}.pdf")

                "IMG_COMPRESS" -> imageProcessor.compressImage(uris[0], currentOptions.quality, currentOptions.format, "compressed_${System.currentTimeMillis()}")
                "IMG_RESIZE" -> imageProcessor.resizeImage(uris[0], currentOptions.width, currentOptions.height, "resized_${System.currentTimeMillis()}", currentOptions.keepAspectRatio, currentOptions.format)
                "IMG_CONVERT" -> imageProcessor.convertImage(uris[0], currentOptions.format, "converted_${System.currentTimeMillis()}")
                "IMG_METADATA" -> imageProcessor.stripMetadata(uris[0], "stripped_${System.currentTimeMillis()}")

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

    // NEW: previously every non-merge tool only ever touched uris[0] — selecting
    // 5 images to compress silently dropped 4 of them. This loops the whole
    // selection and reports a combined before/after size.
    private suspend fun processImageBulk(toolId: String, uris: List<Uri>, options: FlowState.Options) {
        val outputs = mutableListOf<File>()
        var failure: Throwable? = null

        uris.forEachIndexed { index, uri ->
            _flowState.value = FlowState.Processing(
                progress = (index + 1f) / uris.size,
                status = "Processing ${index + 1} of ${uris.size}..."
            )

            val result = when (toolId) {
                "IMG_COMPRESS" -> imageProcessor.compressImage(uri, options.quality, options.format, "compressed_${System.currentTimeMillis()}_$index")
                "IMG_RESIZE" -> imageProcessor.resizeImage(uri, options.width, options.height, "resized_${System.currentTimeMillis()}_$index", options.keepAspectRatio, options.format)
                "IMG_CONVERT" -> imageProcessor.convertImage(uri, options.format, "converted_${System.currentTimeMillis()}_$index")
                "IMG_METADATA" -> imageProcessor.stripMetadata(uri, "stripped_${System.currentTimeMillis()}_$index")
                else -> kotlin.Result.failure(Exception("Unsupported bulk tool: $toolId"))
            }

            result.onSuccess { outputs.add(it) }
                .onFailure { failure = it }
        }

        if (outputs.isEmpty()) {
            _flowState.value = FlowState.Error(failure?.message ?: "Bulk processing failed")
            return
        }

        outputs.forEach { output ->
            fileDao.insertFile(
                ProcessedFile(
                    name = output.name,
                    timestamp = System.currentTimeMillis(),
                    size = output.length(),
                    type = "IMAGE",
                    tool = toolId.replace("IMG_", ""),
                    uri = Uri.fromFile(output).toString()
                )
            )
        }

        _flowState.value = FlowState.Result(
            originalSize = getUrisSize(uris),
            resultSize = outputs.sumOf { it.length() },
            outputUri = Uri.fromFile(outputs.first()),
            multipleOutputs = outputs.map { Uri.fromFile(it) }
        )
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