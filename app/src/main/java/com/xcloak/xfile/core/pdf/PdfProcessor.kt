package com.xcloak.xfile.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.util.Matrix
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        PDFBoxResourceLoader.init(context)
    }

    /**
     * Renders multiple pages of a PDF to allow scrolling preview.
     * Quality is kept low (72 DPI) to preserve memory while providing a good preview.
     */
    suspend fun getPdfPages(uri: Uri, maxPages: Int = 20): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext emptyList()
            val renderer = PdfRenderer(pfd)
            val pagesToRender = renderer.pageCount.coerceAtMost(maxPages)
            
            for (i in 0 until pagesToRender) {
                val page = renderer.openPage(i)
                // Downscale for preview to save memory. 72dpi standard.
                val width = (page.width * 1.5).toInt()
                val height = (page.height * 1.5).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            // Log or handle
        }
        bitmaps
    }

    // Keep the single thumbnail method for the "Merge" list cards (faster)
    suspend fun getThumbnail(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        getPdfPages(uri, 1).firstOrNull()
    }

    suspend fun mergePdfs(
        uris: List<Uri>,
        outputFileName: String,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        val openStreams = mutableListOf<InputStream>()
        try {
            val merger = PDFMergerUtility()
            val outputFile = File(context.cacheDir, outputFileName)
            val outputStream = FileOutputStream(outputFile)

            merger.destinationStream = outputStream

            uris.forEachIndexed { index, uri ->
                context.contentResolver.openInputStream(uri)?.let { inputStream ->
                    merger.addSource(inputStream)
                    openStreams.add(inputStream)
                }
                onProgress(0.1f + (index.toFloat() / uris.size) * 0.7f)
            }

            // Using main memory only to avoid permission issues with temp files on some Android versions
            // and keeping it efficient for typical mobile use cases.
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly())
            outputStream.close()
            onProgress(0.9f)

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            openStreams.forEach { it.close() }
        }
    }

    suspend fun splitPdf(
        uri: Uri,
        splitEvery: Int = 1,
        prefix: String = "page",
        onProgress: (Float) -> Unit = {}
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val files = mutableListOf<File>()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, MemoryUsageSetting.setupMainMemoryOnly())
                val totalPages = document.numberOfPages
                val timestamp = System.currentTimeMillis()

                for (i in 0 until totalPages step splitEvery) {
                    val newDocument = PDDocument()
                    val endPage = (i + splitEvery).coerceAtMost(totalPages)
                    
                    for (j in i until endPage) {
                        newDocument.addPage(document.getPage(j))
                    }

                    val rangeText = if (splitEvery == 1) "${i + 1}" else "${i + 1}-${endPage}"
                    val fileName = "${prefix}_${rangeText}_$timestamp.pdf"
                    val outputFile = File(context.cacheDir, fileName)
                    
                    FileOutputStream(outputFile).use { os -> newDocument.save(os) }
                    newDocument.close()
                    files.add(outputFile)
                    
                    onProgress((i + splitEvery).toFloat() / totalPages)
                }
                document.close()
            }
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractPages(uri: Uri, pageRange: String, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, MemoryUsageSetting.setupMainMemoryOnly())
                val newDocument = PDDocument()
                val pagesToExtract = parsePageRange(pageRange, document.numberOfPages)

                pagesToExtract.forEach { pageIndex ->
                    newDocument.addPage(document.getPage(pageIndex))
                }

                FileOutputStream(outputFile).use { os -> newDocument.save(os) }
                newDocument.close()
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePages(uri: Uri, pageRange: String, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, MemoryUsageSetting.setupMainMemoryOnly())
                val pagesToDelete = parsePageRange(pageRange, document.numberOfPages).toSet()
                val newDocument = PDDocument()

                for (i in 0 until document.numberOfPages) {
                    if (i !in pagesToDelete) {
                        newDocument.addPage(document.getPage(i))
                    }
                }

                FileOutputStream(outputFile).use { os -> newDocument.save(os) }
                newDocument.close()
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rotatePages(uri: Uri, pageRange: String, degrees: Int, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, MemoryUsageSetting.setupMainMemoryOnly())
                val pagesToRotate = if (pageRange.isEmpty()) (0 until document.numberOfPages).toList() else parsePageRange(pageRange, document.numberOfPages)

                pagesToRotate.forEach { pageIndex ->
                    val page = document.getPage(pageIndex)
                    page.rotation = (page.rotation + degrees) % 360
                }

                FileOutputStream(outputFile).use { os -> document.save(os) }
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reorderPages(uri: Uri, newOrder: List<Int>, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, MemoryUsageSetting.setupMainMemoryOnly())
                val newDocument = PDDocument()

                newOrder.forEach { pageIndex ->
                    if (pageIndex in 0 until document.numberOfPages) {
                        newDocument.addPage(document.getPage(pageIndex))
                    }
                }

                FileOutputStream(outputFile).use { os -> newDocument.save(os) }
                newDocument.close()
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addWatermark(
        uri: Uri,
        text: String,
        outputFileName: String,
        opacity: Float = 0.5f,
        rotation: Int = 45,
        fontSize: Float = 48f,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, MemoryUsageSetting.setupMainMemoryOnly())
                val font = PDType1Font.HELVETICA_BOLD
                val totalPages = document.numberOfPages

                document.pages.forEachIndexed { index, page ->
                    val pageSize = page.mediaBox
                    val contentStream = PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)

                    val gs = PDExtendedGraphicsState()
                    gs.nonStrokingAlphaConstant = opacity
                    contentStream.setGraphicsStateParameters(gs)

                    contentStream.beginText()
                    contentStream.setFont(font, fontSize)
                    contentStream.setNonStrokingColor(200, 200, 200)

                    val x = pageSize.width / 2
                    val y = pageSize.height / 2
                    
                    val textWidth = font.getStringWidth(text) / 1000 * fontSize
                    val textHeight = fontSize * 0.7f // Approximate height

                    // Adjust for text centering
                    val matrix = Matrix.getRotateInstance(Math.toRadians(rotation.toDouble()), x, y)
                    matrix.translate(-textWidth / 2, -textHeight / 2)
                    
                    contentStream.setTextMatrix(matrix)
                    contentStream.showText(text)
                    contentStream.endText()
                    contentStream.close()
                    
                    onProgress((index + 1f) / totalPages)
                }

                FileOutputStream(outputFile).use { os -> document.save(os) }
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun compressPdf(
        uri: Uri,
        quality: Int,
        outputFileName: String,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            val jpegQuality = (quality.coerceIn(1, 100)) / 100f

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, MemoryUsageSetting.setupMainMemoryOnly())
                val totalPages = document.pages.count()

                document.pages.forEachIndexed { index, page ->
                    val resources = page.resources ?: return@forEachIndexed
                    for (name in resources.xObjectNames.toList()) {
                        val xObject = resources.getXObject(name)
                        if (xObject is PDImageXObject) {
                            val bitmap = xObject.image
                            val recompressed = JPEGFactory.createFromImage(document, bitmap, jpegQuality)
                            resources.put(name, recompressed)
                        }
                    }
                    onProgress(0.1f + (index.toFloat() / totalPages) * 0.8f)
                }

                FileOutputStream(outputFile).use { os -> document.save(os) }
                document.close()
                onProgress(1.0f)
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPageNumbers(
        uri: Uri,
        outputFileName: String,
        startAt: Int = 1,
        fontSize: Float = 10f,
        position: String = "Bottom Center",
        margin: Float = 20f,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream, MemoryUsageSetting.setupMainMemoryOnly())
                val font = PDType1Font.HELVETICA
                val totalPages = document.numberOfPages

                document.pages.forEachIndexed { index, page ->
                    val pageNumber = startAt + index
                    val text = pageNumber.toString()
                    val pageSize = page.mediaBox
                    val textWidth = font.getStringWidth(text) / 1000 * fontSize
                    val textHeight = fontSize * 0.7f

                    val x = when {
                        position.contains("Left") -> margin
                        position.contains("Right") -> pageSize.width - textWidth - margin
                        else -> (pageSize.width - textWidth) / 2 // Center
                    }

                    val y = when {
                        position.contains("Top") -> pageSize.height - textHeight - margin
                        else -> margin // Bottom
                    }

                    PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                        cs.beginText()
                        cs.setFont(font, fontSize)
                        cs.setNonStrokingColor(80, 80, 80)
                        cs.newLineAtOffset(x, y)
                        cs.showText(text)
                        cs.endText()
                    }
                    onProgress((index + 1f) / totalPages)
                }

                FileOutputStream(outputFile).use { os -> document.save(os) }
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun convertImagesToPdf(
        uris: List<Uri>,
        outputFileName: String,
        pageSize: String = "Original",
        imageScale: String = "Fit",
        quality: Int = 90,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            val document = PDDocument()
            val jpegQuality = quality.coerceIn(1, 100) / 100f

            uris.forEachIndexed { index, uri ->
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@forEachIndexed
                    
                    val mediaBox = if (pageSize == "A4") {
                        PDRectangle.A4
                    } else {
                        PDRectangle(bitmap.width.toFloat(), bitmap.height.toFloat())
                    }
                    
                    val page = PDPage(mediaBox)
                    document.addPage(page)

                    val image = JPEGFactory.createFromImage(document, bitmap, jpegQuality)
                    
                    PDPageContentStream(document, page).use { cs ->
                        if (pageSize == "Original" || imageScale == "Stretch") {
                            cs.drawImage(image, 0f, 0f, mediaBox.width, mediaBox.height)
                        } else {
                            // Fit or Fill logic for A4/fixed size
                            val scaleX = mediaBox.width / bitmap.width
                            val scaleY = mediaBox.height / bitmap.height
                            val scale = if (imageScale == "Fit") kotlin.math.min(scaleX, scaleY) else kotlin.math.max(scaleX, scaleY)
                            
                            val w = bitmap.width * scale
                            val h = bitmap.height * scale
                            val x = (mediaBox.width - w) / 2
                            val y = (mediaBox.height - h) / 2
                            
                            cs.drawImage(image, x, y, w, h)
                        }
                    }
                }
                onProgress((index + 1f) / uris.size)
            }

            if (document.numberOfPages == 0) {
                document.close()
                return@withContext Result.failure(Exception("No images could be read"))
            }

            FileOutputStream(outputFile).use { os -> document.save(os) }
            document.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parsePageRange(range: String, totalPages: Int): List<Int> {
        val result = mutableSetOf<Int>()
        val parts = range.split(",").map { it.trim() }
        for (part in parts) {
            if (part.contains("-")) {
                val subParts = part.split("-")
                if (subParts.size == 2) {
                    val start = subParts[0].toIntOrNull()?.minus(1) ?: continue
                    val end = subParts[1].toIntOrNull()?.minus(1) ?: continue
                    for (i in start..end) {
                        if (i in 0 until totalPages) result.add(i)
                    }
                }
            } else {
                val page = part.toIntOrNull()?.minus(1) ?: continue
                if (page in 0 until totalPages) result.add(page)
            }
        }
        return result.sorted()
    }
}
