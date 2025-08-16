package tech.kaustubhdeshpande.dropnest.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Handles file operations for persistent storage
 */
class FileManager(private val context: Context) {

    /**
     * Saves a file from a URI to app's internal storage
     * @return Path to the saved file or null on failure
     */
    suspend fun saveFileToAppStorage(sourceUri: Uri, fileType: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Get input stream from content resolver
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                    ?: return@withContext null

                // Create directory for file type if it doesn't exist
                val fileDir = File(context.filesDir, fileType.lowercase())
                if (!fileDir.exists()) {
                    fileDir.mkdirs()
                }

                // Generate file name based on timestamp to ensure uniqueness
                val fileName = "file_${System.currentTimeMillis()}"
                val extension = getFileExtension(sourceUri)
                val file = File(fileDir, "$fileName.$extension")

                // Copy the file data
                FileOutputStream(file).use { outputStream ->
                    inputStream.use { input ->
                        val buffer = ByteArray(4 * 1024) // 4KB buffer
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                        }
                        outputStream.flush()
                    }
                }

                Log.d("FileManager", "File saved to ${file.absolutePath}")
                return@withContext file.absolutePath
            } catch (e: IOException) {
                Log.e("FileManager", "Error saving file", e)
                return@withContext null
            }
        }
    }

    /**
     * Gets file extension from URI
     */
    private fun getFileExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        return when {
            mimeType.contains("image/jpeg") -> "jpg"
            mimeType.contains("image/png") -> "png"
            mimeType.contains("application/pdf") -> "pdf"
            else -> {
                // Try to extract extension from the URI path
                uri.path?.let { path ->
                    if (path.contains(".")) {
                        return path.substring(path.lastIndexOf(".") + 1)
                    }
                }
                // Default extension
                "dat"
            }
        }
    }

    /**
     * Gets the display name of a file from URI
     */
    suspend fun getFileName(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val cursor = context.contentResolver.query(
                    uri,
                    arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                    null, null, null
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            return@withContext it.getString(index)
                        }
                    }
                }

                // Fallback: use last path segment or timestamp
                uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
            } catch (e: Exception) {
                Log.e("FileManager", "Error getting file name", e)
                "file_${System.currentTimeMillis()}"
            }
        }
    }
}