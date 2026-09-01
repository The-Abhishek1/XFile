package com.xcloak.xfile.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
    suspend fun resizeImage(
        uri: Uri,
        width: Int,
        height: Int,
        outputFileName: String,
        keepAspectRatio: Boolean = true
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
                
                val outputFile = File(context.cacheDir, outputFileName)
                val outputStream = FileOutputStream(outputFile)
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
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
                val outputFile = File(context.cacheDir, outputFileName)
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
                val outputFile = File(context.cacheDir, outputFileName)
                val outputStream = FileOutputStream(outputFile)
                bitmap.compress(format, 90, outputStream)
                outputStream.close()
                Result.success(outputFile)
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stripMetadata(uri: Uri, outputFileName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(context.cacheDir, outputFileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = FileOutputStream(outputFile)
                // Compressing to JPEG without metadata
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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
