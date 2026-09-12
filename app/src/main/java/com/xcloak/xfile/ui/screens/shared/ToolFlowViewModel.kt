package com.xcloak.xfile.ui.screens.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xcloak.xfile.core.database.FileDao
import com.xcloak.xfile.core.database.ProcessedFile
import com.xcloak.xfile.core.image.ImageProcessor
import com.xcloak.xfile.core.pdf.PdfProcessor
import com.xcloak.xfile.core.ads.AdManager
import com.xcloak.xfile.core.billing.BillingManager
import com.xcloak.xfile.core.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val watermarkText: String = "",
        val outputFileName: String = "",
        val selectedFileNames: List<String> = emptyList(),
        val splitEveryN: Int = 1,
        val outputPrefix: String = "split",
        val watermarkOpacity: Float = 0.5f,
        val watermarkRotation: Int = 45,
        val watermarkSize: Float = 48f,
        val numbersStartAt: Int = 1,
        val numbersFontSize: Float = 10f,
        val numbersPosition: String = "Bottom Center",
        val convertPageSize: String = "Original",
        val convertImageScale: String = "Fit",
        val flipHorizontal: Boolean = false,
        val flipVertical: Boolean = false,
        val thumbnails: Map<Uri, List<Bitmap>> = emptyMap()
    ) : FlowState
    data class Processing(val progress: Float, val status: String) : FlowState
    data class Result(
        val originalSize: Long,
        val resultSize: Long,
        // FIX: this used to be a Uri built with Uri.fromFile(output). Handing a
        // file:// Uri to another app via an Intent (Share/Open) throws
        // FileUriExposedException on API 24+, and Save had nowhere to copy from
        // once outside the view model. Keeping the real Files lets the screen
        // route everything through FileActions/FileProvider instead.
        val outputFiles: List<File>
    ) : FlowState {
        val primaryFile: File get() = outputFiles.first()
    }
    data class Error(val message: String) : FlowState
}

@HiltViewModel
class ToolFlowViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfProcessor: PdfProcessor,
    private val imageProcessor: ImageProcessor,
    private val fileDao: FileDao,
    private val notificationHelper: NotificationHelper,
    val adManager: AdManager,
    val billingManager: BillingManager
) : ViewModel() {

    private val _flowState = MutableStateFlow<FlowState>(FlowState.Idle)
    val flowState: StateFlow<FlowState> = _flowState.asStateFlow()

    // Tools that make sense to run across every selected file, not just the first.
    // Merge and Convert-to-PDF already consume the whole list by design.
    private val bulkCapableImageTools = setOf("IMG_COMPRESS", "IMG_RESIZE", "IMG_CONVERT", "IMG_METADATA", "IMG_ROTATE", "IMG_FLIP")

    fun setFiles(uris: List<Uri>) {
        // FIX: was checking uris[0].toString().contains("image"), which almost never
        // matches real content:// document-picker Uris (they're opaque IDs, not paths
        // with the word "image" in them) — so the Privacy tool's "Detected Metadata"
        // list was silently empty in practice. Checking the actual MIME type is reliable.
        val isImage = uris.isNotEmpty() &&
                context.contentResolver.getType(uris[0])?.startsWith("image/") == true
        val metadata = if (isImage) imageProcessor.getMetadataSummary(uris[0]) else emptyMap()
        val names = uris.map { getDisplayName(it) }

        _flowState.value = FlowState.Options(uris, metadataSummary = metadata, selectedFileNames = names)
        
        loadThumbnails(uris)
    }

    private fun loadThumbnails(uris: List<Uri>) {
        viewModelScope.launch {
            val current = _flowState.value as? FlowState.Options ?: return@launch
            val newThumbnails = current.thumbnails.toMutableMap()
            
            uris.forEach { uri ->
                if (!newThumbnails.containsKey(uri)) {
                    val mimeType = context.contentResolver.getType(uri)
                    val bitmaps = if (mimeType?.startsWith("image/") == true) {
                        val b = loadSmallBitmap(uri)
                        if (b != null) listOf(b) else emptyList()
                    } else if (mimeType == "application/pdf") {
                        pdfProcessor.getPdfPages(uri, maxPages = 15)
                    } else emptyList()
                    
                    newThumbnails[uri] = bitmaps
                    
                    // Update state incrementally for better perceived performance
                    val updated = _flowState.value as? FlowState.Options
                    if (updated != null) {
                        _flowState.value = updated.copy(thumbnails = newThumbnails.toMap())
                    }
                }
            }
        }
    }

    private suspend fun loadSmallBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(input, null, options)
                
                val reqWidth = 500
                val reqHeight = 500
                var inSampleSize = 1
                if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                    val halfHeight: Int = options.outHeight / 2
                    val halfWidth: Int = options.outWidth / 2
                    while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                        inSampleSize *= 2
                    }
                }
                
                context.contentResolver.openInputStream(uri)?.use { input2 ->
                    val finalOptions = BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                    }
                    BitmapFactory.decodeStream(input2, null, finalOptions)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updateOptions(
        pageRange: String? = null,
        rotationDegrees: Int? = null,
        quality: Int? = null,
        format: Bitmap.CompressFormat? = null,
        width: Int? = null,
        height: Int? = null,
        keepAspectRatio: Boolean? = null,
        watermarkText: String? = null,
        outputFileName: String? = null,
        splitEveryN: Int? = null,
        outputPrefix: String? = null,
        watermarkOpacity: Float? = null,
        watermarkRotation: Int? = null,
        watermarkSize: Float? = null,
        numbersStartAt: Int? = null,
        numbersFontSize: Float? = null,
        numbersPosition: String? = null,
        convertPageSize: String? = null,
        convertImageScale: String? = null,
        flipHorizontal: Boolean? = null,
        flipVertical: Boolean? = null
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
                watermarkText = watermarkText ?: current.watermarkText,
                outputFileName = outputFileName ?: current.outputFileName,
                splitEveryN = splitEveryN ?: current.splitEveryN,
                outputPrefix = outputPrefix ?: current.outputPrefix,
                watermarkOpacity = watermarkOpacity ?: current.watermarkOpacity,
                watermarkRotation = watermarkRotation ?: current.watermarkRotation,
                watermarkSize = watermarkSize ?: current.watermarkSize,
                numbersStartAt = numbersStartAt ?: current.numbersStartAt,
                numbersFontSize = numbersFontSize ?: current.numbersFontSize,
                numbersPosition = numbersPosition ?: current.numbersPosition,
                convertPageSize = convertPageSize ?: current.convertPageSize,
                convertImageScale = convertImageScale ?: current.convertImageScale,
                flipHorizontal = flipHorizontal ?: current.flipHorizontal,
                flipVertical = flipVertical ?: current.flipVertical
            )
        }
    }

    fun reorderFiles(fromIndex: Int, toIndex: Int) {
        val current = _flowState.value
        if (current is FlowState.Options) {
            val newList = current.selectedUris.toMutableList()
            val newNames = current.selectedFileNames.toMutableList()
            if (fromIndex in newList.indices && toIndex in newList.indices) {
                val item = newList.removeAt(fromIndex)
                newList.add(toIndex, item)
                
                if (fromIndex in newNames.indices && toIndex in newNames.indices) {
                    val nameItem = newNames.removeAt(fromIndex)
                    newNames.add(toIndex, nameItem)
                }
                
                _flowState.value = current.copy(selectedUris = newList, selectedFileNames = newNames)
            }
        }
    }

    fun removeFile(index: Int) {
        val current = _flowState.value
        if (current is FlowState.Options) {
            val newList = current.selectedUris.toMutableList()
            val newNames = current.selectedFileNames.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
                if (newNames.size > index) newNames.removeAt(index)
                
                if (newList.isEmpty()) {
                    _flowState.value = FlowState.Idle
                } else {
                    _flowState.value = current.copy(selectedUris = newList, selectedFileNames = newNames)
                }
            }
        }
    }

    fun startProcessing(toolId: String) {
        val currentOptions = _flowState.value as? FlowState.Options ?: return
        val uris = currentOptions.selectedUris
        if (uris.isEmpty()) return

        viewModelScope.launch {
            _flowState.value = FlowState.Processing(0.01f, "Initializing...")

            // Tools that take the whole file list at once (merge, image->pdf) or
            // only ever act on a single file (split, extract, etc.) go through the
            // single-result path below. Bulk-capable image tools loop separately.
            if (toolId in bulkCapableImageTools && uris.size > 1) {
                processImageBulk(toolId, uris, currentOptions)
                notificationHelper.showWorkCompletedNotification(toolId.replace("_", " "))
                return@launch
            }

            val result: kotlin.Result<*> = when (toolId) {
                "PDF_MERGE" -> pdfProcessor.mergePdfs(
                    uris,
                    getPdfOutputName(currentOptions, "merged"),
                    onProgress = { _flowState.value = FlowState.Processing(it, "Merging PDFs...") }
                )
                "PDF_SPLIT" -> {
                    val prefix = if (currentOptions.outputPrefix.isNotBlank()) currentOptions.outputPrefix else "split"
                    pdfProcessor.splitPdf(
                        uris[0],
                        currentOptions.splitEveryN,
                        prefix,
                        onProgress = { _flowState.value = FlowState.Processing(it, "Splitting PDF...") }
                    )
                }
                "PDF_EXTRACT" -> pdfProcessor.extractPages(uris[0], currentOptions.pageRange, getPdfOutputName(currentOptions, "extracted"))
                "PDF_DELETE" -> pdfProcessor.deletePages(uris[0], currentOptions.pageRange, getPdfOutputName(currentOptions, "deleted"))
                "PDF_ROTATE" -> pdfProcessor.rotatePages(uris[0], currentOptions.pageRange, currentOptions.rotationDegrees, getPdfOutputName(currentOptions, "rotated"))
                "PDF_WATERMARK" -> pdfProcessor.addWatermark(
                    uris[0],
                    currentOptions.watermarkText,
                    getPdfOutputName(currentOptions, "watermarked"),
                    currentOptions.watermarkOpacity,
                    currentOptions.watermarkRotation,
                    currentOptions.watermarkSize,
                    onProgress = { _flowState.value = FlowState.Processing(it, "Adding Watermark...") }
                )
                "PDF_COMPRESS" -> pdfProcessor.compressPdf(
                    uris[0],
                    currentOptions.quality,
                    getPdfOutputName(currentOptions, "compressed"),
                    onProgress = { _flowState.value = FlowState.Processing(it, "Compressing PDF...") }
                )
                "PDF_NUMBERS" -> pdfProcessor.addPageNumbers(
                    uris[0],
                    getPdfOutputName(currentOptions, "numbered"),
                    currentOptions.numbersStartAt,
                    currentOptions.numbersFontSize,
                    currentOptions.numbersPosition,
                    onProgress = { _flowState.value = FlowState.Processing(it, "Adding Page Numbers...") }
                )
                "PDF_CONVERT" -> pdfProcessor.convertImagesToPdf(
                    uris,
                    getPdfOutputName(currentOptions, "converted"),
                    currentOptions.convertPageSize,
                    currentOptions.convertImageScale,
                    currentOptions.quality,
                    onProgress = { _flowState.value = FlowState.Processing(it, "Converting to PDF...") }
                )

                "IMG_COMPRESS" -> imageProcessor.compressImage(uris[0], currentOptions.quality, currentOptions.format, getImageOutputName(currentOptions, "compressed"))
                "IMG_RESIZE" -> imageProcessor.resizeImage(uris[0], currentOptions.width, currentOptions.height, getImageOutputName(currentOptions, "resized"), currentOptions.keepAspectRatio, currentOptions.format)
                "IMG_CONVERT" -> imageProcessor.convertImage(uris[0], currentOptions.format, getImageOutputName(currentOptions, "converted"))
                "IMG_METADATA" -> imageProcessor.stripMetadata(uris[0], getImageOutputName(currentOptions, "stripped"), currentOptions.format)
                "IMG_ROTATE" -> imageProcessor.rotateImage(uris[0], currentOptions.rotationDegrees, getImageOutputName(currentOptions, "rotated"), currentOptions.format)
                "IMG_FLIP" -> imageProcessor.flipImage(uris[0], currentOptions.flipHorizontal, currentOptions.flipVertical, getImageOutputName(currentOptions, "flipped"), currentOptions.format)

                else -> Result.failure<File>(Exception("Unknown tool: $toolId"))
            }

            _flowState.value = FlowState.Processing(0.95f, "Finalizing...")

            result.onSuccess { output ->
                notificationHelper.showWorkCompletedNotification(toolId.replace("_", " "))
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
                        outputFiles = listOf(output)
                    )
                } else if (output is List<*>) {
                    val files = output.filterIsInstance<File>()
                    _flowState.value = FlowState.Result(
                        originalSize = getUrisSize(uris),
                        resultSize = files.sumOf { it.length() },
                        outputFiles = files
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

            val baseName = if (options.outputFileName.isNotBlank()) "${options.outputFileName}_$index" 
                            else "processed_${System.currentTimeMillis()}_$index"

            val result = when (toolId) {
                "IMG_COMPRESS" -> imageProcessor.compressImage(uri, options.quality, options.format, baseName)
                "IMG_RESIZE" -> imageProcessor.resizeImage(uri, options.width, options.height, baseName, options.keepAspectRatio, options.format)
                "IMG_CONVERT" -> imageProcessor.convertImage(uri, options.format, baseName)
                "IMG_METADATA" -> imageProcessor.stripMetadata(uri, baseName, options.format)
                "IMG_ROTATE" -> imageProcessor.rotateImage(uri, options.rotationDegrees, baseName, options.format)
                "IMG_FLIP" -> imageProcessor.flipImage(uri, options.flipHorizontal, options.flipVertical, baseName, options.format)
                else -> Result.failure(Exception("Unsupported bulk tool: $toolId"))
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
            outputFiles = outputs
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

    private fun getPdfOutputName(options: FlowState.Options, defaultPrefix: String): String {
        return if (options.outputFileName.isNotBlank()) {
            if (options.outputFileName.lowercase().endsWith(".pdf")) options.outputFileName
            else "${options.outputFileName}.pdf"
        } else {
            "${defaultPrefix}_${System.currentTimeMillis()}.pdf"
        }
    }

    private fun getImageOutputName(options: FlowState.Options, defaultPrefix: String): String {
        return if (options.outputFileName.isNotBlank()) {
            options.outputFileName
        } else {
            "${defaultPrefix}_${System.currentTimeMillis()}"
        }
    }

    private fun getDisplayName(uri: Uri): String {
        var name = ""
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        if (name.isEmpty()) {
            name = uri.path?.substringAfterLast('/') ?: "unknown"
        }
        return name
    }

    fun reset() {
        _flowState.value = FlowState.Idle
    }

    fun showDownloadNotification(fileName: String) {
        notificationHelper.showDownloadNotification("File Saved", "$fileName saved to Downloads")
    }
}
