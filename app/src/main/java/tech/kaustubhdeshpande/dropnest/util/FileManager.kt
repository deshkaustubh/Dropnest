package tech.kaustubhdeshpande.dropnest.util

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
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
                val fileName = getFileName(sourceUri) ?: "file_${System.currentTimeMillis()}"
                val extension = getFileExtension(sourceUri)

                // Clean up the filename to remove problematic characters
                val cleanFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")

                // Create the final file name with extension if needed
                val finalFileName = if (cleanFileName.contains(".")) {
                    cleanFileName
                } else {
                    "$cleanFileName.$extension"
                }

                val file = File(fileDir, finalFileName)

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
    fun getFileExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.contains("image/jpeg") == true -> "jpg"
            mimeType?.contains("image/png") == true -> "png"
            mimeType?.contains("application/pdf") == true -> "pdf"
            mimeType?.contains("application/msword") == true -> "doc"
            mimeType?.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document") == true -> "docx"
            mimeType?.contains("application/vnd.ms-excel") == true -> "xls"
            mimeType?.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") == true -> "xlsx"
            mimeType?.contains("application/vnd.ms-powerpoint") == true -> "ppt"
            mimeType?.contains("application/vnd.openxmlformats-officedocument.presentationml.presentation") == true -> "pptx"
            mimeType?.contains("text/plain") == true -> "txt"
            else -> {
                // Try to extract extension from the file name
                val fileName = getFileNameSync(uri)
                if (!fileName.isNullOrEmpty() && fileName.contains(".")) {
                    return fileName.substring(fileName.lastIndexOf(".") + 1)
                }
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
     * Gets MIME type from a URI
     */
    fun getMimeType(uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            return mimeType
        }

        // Try to determine MIME type from extension
        val extension = getFileExtension(uri)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    /**
     * Gets the display name of a file from URI synchronously
     */
    private fun getFileNameSync(uri: Uri): String? {
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
                        return it.getString(index)
                    }
                }
            }

            // Fallback: use last path segment or timestamp
            return uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            Log.e("FileManager", "Error getting file name", e)
            return "file_${System.currentTimeMillis()}"
        }
    }

    /**
     * Gets the display name of a file from URI
     */
    suspend fun getFileName(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            getFileNameSync(uri)
        }
    }

    /**
     * Gets a display name for a document type based on MIME type and file extension
     */
    fun getDocumentTypeName(uri: Uri, mimeType: String?): String {
        val ext = getFileExtension(uri).lowercase()
        return when {
            mimeType == null && ext.isBlank() -> "DOC"
            // IMAGE
            mimeType?.startsWith("image") == true || ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic") -> "IMG"
            // PDF
            mimeType?.contains("pdf") == true || ext == "pdf" -> "PDF"
            // WORD
            mimeType?.contains("msword") == true || ext == "doc" -> "DOC"
            mimeType?.contains("officedocument.wordprocessingml.document") == true || ext == "docx" -> "DOC"
            // EXCEL
            mimeType?.contains("excel") == true || mimeType?.contains("sheet") == true || ext in listOf("xls", "xlsx", "csv") -> "Excel"
            // POWERPOINT
            mimeType?.contains("powerpoint") == true || mimeType?.contains("presentation") == true || ext in listOf("ppt", "pptx") -> "PPT"
            // TEXT
            mimeType?.contains("text/plain") == true || ext in listOf("txt", "md", "rtf") -> "TXT"
            // ARCHIVE
            ext in listOf("zip", "rar", "7z", "tar", "gz") -> "Archive"
            else -> "DOC"
        }
    }
}