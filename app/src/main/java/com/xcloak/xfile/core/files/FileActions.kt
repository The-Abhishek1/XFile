package com.xcloak.xfile.core.files

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Everything ResultScreen's Share/Save/Open buttons and FilesScreen's row actions need.
 *
 * Output files live in the app's cacheDir (see PdfProcessor/ImageProcessor), so a plain
 * file:// Uri handed to another app's Intent throws FileUriExposedException on API 24+.
 * Every path here goes through the app's FileProvider (see AndroidManifest + file_paths.xml)
 * to get a content:// Uri instead.
 */
object FileActions {

    private const val AUTHORITY_SUFFIX = ".fileprovider"

    fun contentUriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, context.packageName + AUTHORITY_SUFFIX, file)

    fun mimeTypeFor(file: File): String {
        val extension = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: if (extension == "pdf") "application/pdf" else "application/octet-stream"
    }

    fun share(context: Context, files: List<File>) {
        if (files.isEmpty()) return
        val uris = ArrayList(files.map { contentUriFor(context, it) })
        val mimeType = files.map { mimeTypeFor(it) }.distinct().singleOrNull() ?: "*/*"

        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uris.first())
                type = mimeType
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                type = mimeType
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Share via")
        if (context !is android.app.Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun open(context: Context, file: File) {
        val uri = contentUriFor(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeTypeFor(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies into the public Downloads collection on API 29+ (no permission needed).
     * On API 26-28 (minSdk) writing to public storage needs a runtime-granted
     * WRITE_EXTERNAL_STORAGE permission we don't otherwise ask for, so instead this saves
     * to the app's own external files dir, which needs no permission — the file is still
     * reachable afterwards from the in-app Files tab, Share, or Open.
     */
    fun save(context: Context, files: List<File>): Boolean {
        if (files.isEmpty()) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            files.all { saveToDownloadsQPlus(context, it) }
        } else {
            files.all { saveToAppExternalFiles(context, it) }
        }
    }

    private fun saveToDownloadsQPlus(context: Context, file: File): Boolean {
        return try {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeTypeFor(file))
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/XFile")
            }
            val destUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return false
            resolver.openOutputStream(destUri)?.use { out ->
                FileInputStream(file).use { input -> input.copyTo(out) }
            } ?: return false
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun saveToAppExternalFiles(context: Context, file: File): Boolean {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return false
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, file.name)
            FileOutputStream(dest).use { out ->
                FileInputStream(file).use { input -> input.copyTo(out) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}