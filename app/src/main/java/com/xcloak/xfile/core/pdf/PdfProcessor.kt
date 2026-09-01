package com.xcloak.xfile.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        PDFBoxResourceLoader.init(context)
    }

    suspend fun mergePdfs(uris: List<Uri>, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val merger = PDFMergerUtility()
            val outputFile = File(context.cacheDir, outputFileName)
            val outputStream = FileOutputStream(outputFile)

            merger.destinationStream = outputStream

            uris.forEach { uri ->
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    merger.addSource(inputStream)
                }
            }

            merger.mergeDocuments(null)
            outputStream.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun splitPdf(uri: Uri): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val files = mutableListOf<File>()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)
                for (i in 0 until document.numberOfPages) {
                    val newDocument = PDDocument()
                    newDocument.addPage(document.getPage(i))
                    val outputFile = File(context.cacheDir, "page_${i + 1}_${System.currentTimeMillis()}.pdf")
                    FileOutputStream(outputFile).use { os -> newDocument.save(os) }
                    newDocument.close()
                    files.add(outputFile)
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
                val document = PDDocument.load(inputStream)
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
                val document = PDDocument.load(inputStream)
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
                val document = PDDocument.load(inputStream)
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
                val document = PDDocument.load(inputStream)
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
        rotation: Int = 45
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)
                val font = PDType1Font.HELVETICA_BOLD

                for (page in document.pages) {
                    val contentStream = PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)

                    val gs = PDExtendedGraphicsState()
                    gs.nonStrokingAlphaConstant = opacity
                    contentStream.setGraphicsStateParameters(gs)

                    contentStream.beginText()
                    contentStream.setFont(font, 48f)
                    contentStream.setNonStrokingColor(200, 200, 200)

                    val pageSize = page.mediaBox
                    val x = pageSize.width / 2
                    val y = pageSize.height / 2

                    contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(rotation.toDouble()), x, y))
                    contentStream.showText(text)
                    contentStream.endText()
                    contentStream.close()
                }

                FileOutputStream(outputFile).use { os -> document.save(os) }
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NEW: was missing entirely — the "Compress" tile had no engine method behind it.
    // Downsamples/recompresses embedded raster images page by page; text/vector
    // content is untouched since that's already compact in a PDF.
    suspend fun compressPdf(uri: Uri, quality: Int, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            val jpegQuality = (quality.coerceIn(1, 100)) / 100f

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)

                for (page in document.pages) {
                    val resources = page.resources ?: continue
                    for (name in resources.xObjectNames.toList()) {
                        val xObject = resources.getXObject(name)
                        if (xObject is PDImageXObject) {
                            val bitmap = xObject.image
                            val recompressed = JPEGFactory.createFromImage(document, bitmap, jpegQuality)
                            resources.put(name, recompressed)
                        }
                    }
                }

                FileOutputStream(outputFile).use { os -> document.save(os) }
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NEW: was missing entirely — the "Numbers" tile had no engine method behind it.
    suspend fun addPageNumbers(uri: Uri, outputFileName: String, startAt: Int = 1): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)
                val font = PDType1Font.HELVETICA

                document.pages.forEachIndexed { index, page ->
                    val pageNumber = startAt + index
                    val text = pageNumber.toString()
                    val pageSize = page.mediaBox
                    val fontSize = 10f
                    val textWidth = font.getStringWidth(text) / 1000 * fontSize

                    PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                        cs.beginText()
                        cs.setFont(font, fontSize)
                        cs.setNonStrokingColor(80, 80, 80)
                        cs.newLineAtOffset((pageSize.width - textWidth) / 2, 20f)
                        cs.showText(text)
                        cs.endText()
                    }
                }

                FileOutputStream(outputFile).use { os -> document.save(os) }
                document.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NEW: was missing entirely — the "Image → PDF" tile had no engine method behind it.
    // One image per page, page sized to the image at 1px = 1pt (matches most
    // "image to PDF" tool behavior for on-screen images).
    suspend fun convertImagesToPdf(uris: List<Uri>, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            val document = PDDocument()

            uris.forEach { uri ->
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val page = PDPage(PDRectangle(bitmap.width.toFloat(), bitmap.height.toFloat()))
                    document.addPage(page)

                    val image = JPEGFactory.createFromImage(document, bitmap, 0.9f)
                    PDPageContentStream(document, page).use { cs ->
                        cs.drawImage(image, 0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                    }
                }
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