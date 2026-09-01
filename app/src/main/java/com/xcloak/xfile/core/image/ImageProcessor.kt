package com.xcloak.xfile.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // FIX: every method below used to hardcode ".jpg" filenames/JPEG compression
    // regardless of the format actually requested, which silently flattened
    // transparent PNGs to opaque JPEGs. This derives the real extension.
    private fun Bitmap.CompressFormat.extension(): String = when (this) {
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.WEBP -> "webp"
        else -> "jpg"
    }

    private fun withExtension(fileName: String, format: Bitmap.CompressFormat): String {
        val base = fileName.substringBeforeLast('.', fileName)
        return "$base.${format.extension()}"
    }

    suspend fun resizeImage(
        uri: Uri,
        width: Int,
        height: Int,
        outputFileName: String,
        keepAspectRatio: Boolean = true,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                val finalWidth: Int
                val finalHeight: Int

                if (keepAspectRatio) {
                    val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                    if (width.toFloat() / height.toFloat() > ratio) {
                        finalHeight = height
                        finalWidth = (height * ratio).toInt()
                    } else {
                        finalWidth = width
                        finalHeight = (width / ratio).toInt()
                    }
                } else {
                    finalWidth = width
                    finalHeight = height
                }

                val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)

                val outputFile = File(context.cacheDir, withExtension(outputFileName, format))
                val outputStream = FileOutputStream(outputFile)
                resizedBitmap.compress(format, 90, outputStream)
                outputStream.close()

                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun compressImage(
        uri: Uri,
        quality: Int,
        format: Bitmap.CompressFormat,
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputFile = File(context.cacheDir, withExtension(outputFileName, format))
                val outputStream = FileOutputStream(outputFile)
                bitmap.compress(format, quality, outputStream)
                outputStream.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun convertImage(
        uri: Uri,
        format: Bitmap.CompressFormat,
        outputFileName: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputFile = File(context.cacheDir, withExtension(outputFileName, format))
                val outputStream = FileOutputStream(outputFile)
                bitmap.compress(format, 90, outputStream)
                outputStream.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stripMetadata(
        uri: Uri,
        outputFileName: String,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // FIX: previously always re-encoded as JPEG at 100 quality, which
            // silently dropped transparency on PNGs. Defaults to PNG now (lossless,
            // keeps alpha); pass JPEG explicitly for photos where that's preferred.
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputFile = File(context.cacheDir, withExtension(outputFileName, format))
                val outputStream = FileOutputStream(outputFile)
                bitmap.compress(format, 100, outputStream)
                outputStream.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMetadataSummary(uri: Uri): Map<String, String> {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                mutableMapOf<String, String>().apply {
                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)?.let { put("GPS Location", "Found") }
                    exif.getAttribute(ExifInterface.TAG_MODEL)?.let { put("Camera Model", it) }
                    exif.getAttribute(ExifInterface.TAG_DATETIME)?.let { put("Date Taken", it) }
                    exif.getAttribute(ExifInterface.TAG_SOFTWARE)?.let { put("Software", it) }
                }
            } ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}